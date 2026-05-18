package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.notification.CreateNotificationRequest;
import com.smartresidential.backend.entities.Attachment;
import com.smartresidential.backend.entities.AttachmentProcessingStatus;
import com.smartresidential.backend.repositories.AttachmentRepository;
import com.smartresidential.backend.services.interfaces.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Set;

@Service
public class AttachmentProcessingService {

    private static final Set<String> ALLOWED_EXACT_TYPES = Set.of(
            "application/pdf",
            "text/plain",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/zip"
    );

    private final AttachmentRepository attachmentRepository;
    private final NotificationService notificationService;
    private final Path storageRoot;
    private final long maxFileSizeBytes;

    public AttachmentProcessingService(
            AttachmentRepository attachmentRepository,
            NotificationService notificationService,
            @Value("${app.attachments.storage-dir:uploads/attachments}") String storageDir,
            @Value("${app.attachments.max-file-size-bytes:104857600}") long maxFileSizeBytes
    ) {
        this.attachmentRepository = attachmentRepository;
        this.notificationService = notificationService;
        this.storageRoot = Path.of(storageDir).toAbsolutePath().normalize();
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    @Async("taskExecutor")
    @Transactional
    public void processAttachmentAsync(Long attachmentId, String pendingPathValue) {
        Attachment attachment = attachmentRepository.findById(attachmentId).orElse(null);
        if (attachment == null) {
            return;
        }

        try {
            attachment.setProcessingStatus(AttachmentProcessingStatus.PROCESSING);
            attachmentRepository.save(attachment);

            Path pendingPath = Path.of(pendingPathValue).toAbsolutePath().normalize();
            validateSafePath(pendingPath);
            validateAttachment(attachment, pendingPath);

            Path finalPath = tenantDirectory(pendingPath)
                    .resolve("files")
                    .resolve(attachment.getStoredFilename())
                    .normalize();
            validateSafePath(finalPath);
            Files.createDirectories(finalPath.getParent());
            Files.move(pendingPath, finalPath, StandardCopyOption.REPLACE_EXISTING);

            attachment.setFileUrl(finalPath.toString());
            attachment.setThumbnailPath(generateThumbnailIfImage(attachment, finalPath));
            attachment.setProcessingStatus(AttachmentProcessingStatus.COMPLETED);
            attachment.setProcessingError(null);
            attachment.setProcessedAt(LocalDateTime.now());
            attachmentRepository.save(attachment);

            notifyUploader(attachment, "Attachment processed successfully: " + displayName(attachment), "SUCCESS");
        } catch (Exception e) {
            attachment.setProcessingStatus(AttachmentProcessingStatus.FAILED);
            attachment.setProcessingError(e.getMessage());
            attachment.setProcessedAt(LocalDateTime.now());
            attachmentRepository.save(attachment);

            notifyUploader(attachment, "Attachment processing failed: " + displayName(attachment), "ALERT");
        }
    }

    private void validateAttachment(Attachment attachment, Path pendingPath) throws IOException {
        if (!Files.exists(pendingPath) || Files.size(pendingPath) == 0) {
            throw new IllegalArgumentException("Uploaded file is empty.");
        }

        long actualSize = Files.size(pendingPath);
        if (actualSize > maxFileSizeBytes) {
            throw new IllegalArgumentException("Uploaded file exceeds the maximum allowed size.");
        }

        attachment.setFileSize(actualSize);

        String detectedType = Files.probeContentType(pendingPath);
        String fileType = StringUtils.hasText(detectedType) ? detectedType : attachment.getFileType();
        attachment.setFileType(fileType);

        if (!isAllowedFileType(fileType)) {
            throw new IllegalArgumentException("Unsupported file type.");
        }
    }

    private boolean isAllowedFileType(String fileType) {
        if (!StringUtils.hasText(fileType)) {
            return false;
        }

        String normalized = fileType.toLowerCase();
        return normalized.startsWith("image/")
                || normalized.startsWith("video/")
                || ALLOWED_EXACT_TYPES.contains(normalized);
    }

    private String generateThumbnailIfImage(Attachment attachment, Path filePath) throws IOException {
        String fileType = attachment.getFileType();
        if (!StringUtils.hasText(fileType) || !fileType.toLowerCase().startsWith("image/")) {
            return null;
        }

        BufferedImage original = ImageIO.read(filePath.toFile());
        if (original == null) {
            return null;
        }

        int maxSide = 320;
        double scale = Math.min((double) maxSide / original.getWidth(), (double) maxSide / original.getHeight());
        scale = Math.min(scale, 1.0d);
        int width = Math.max(1, (int) Math.round(original.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(original.getHeight() * scale));

        Image scaled = original.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage thumbnail = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = thumbnail.createGraphics();
        try {
            graphics.drawImage(scaled, 0, 0, null);
        } finally {
            graphics.dispose();
        }

        Path thumbnailPath = tenantDirectory(filePath)
                .resolve("thumbnails")
                .resolve(attachment.getStoredFilename() + ".jpg")
                .normalize();
        validateSafePath(thumbnailPath);
        Files.createDirectories(thumbnailPath.getParent());
        ImageIO.write(thumbnail, "jpg", thumbnailPath.toFile());
        return thumbnailPath.toString();
    }

    private Path tenantDirectory(Path childPath) {
        Path relative = storageRoot.relativize(childPath);
        if (relative.getNameCount() == 0) {
            return storageRoot.resolve("default");
        }
        return storageRoot.resolve(relative.getName(0).toString()).normalize();
    }

    private void validateSafePath(Path path) {
        if (!path.startsWith(storageRoot)) {
            throw new IllegalArgumentException("Invalid attachment storage path.");
        }
    }

    private void notifyUploader(Attachment attachment, String message, String type) {
        if (attachment.getUploadedBy() == null) {
            return;
        }

        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(attachment.getUploadedBy().getId());
        request.setMessage(message);
        request.setType(type);
        notificationService.create(request);
    }

    private String displayName(Attachment attachment) {
        if (StringUtils.hasText(attachment.getOriginalFilename())) {
            return attachment.getOriginalFilename();
        }
        if (StringUtils.hasText(attachment.getFileName())) {
            return attachment.getFileName();
        }
        return "attachment #" + attachment.getId();
    }
}
