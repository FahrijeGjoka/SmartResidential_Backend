package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.attachment.AttachmentResponseDTO;
import com.smartresidential.backend.dto.attachment.CreateAttachmentRequest;
import com.smartresidential.backend.entities.Attachment;
import com.smartresidential.backend.entities.AttachmentProcessingStatus;
import com.smartresidential.backend.entities.Issue;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.exceptions.BadRequestException;
import com.smartresidential.backend.exceptions.ResourceNotFoundException;
import com.smartresidential.backend.multitenancy.TenantContext;
import com.smartresidential.backend.repositories.AttachmentRepository;
import com.smartresidential.backend.repositories.IssueRepository;
import com.smartresidential.backend.repositories.UserRepository;
import com.smartresidential.backend.services.interfaces.AttachmentService;
import com.smartresidential.backend.services.interfaces.AuditLogService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final AttachmentProcessingService attachmentProcessingService;
    private final AuditLogService auditLogService;
    private final Path storageRoot;

    public AttachmentServiceImpl(
            AttachmentRepository attachmentRepository,
            IssueRepository issueRepository,
            UserRepository userRepository,
            AttachmentProcessingService attachmentProcessingService,
            AuditLogService auditLogService,
            @Value("${app.attachments.storage-dir:uploads/attachments}") String storageDir
    ) {
        this.attachmentRepository = attachmentRepository;
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
        this.attachmentProcessingService = attachmentProcessingService;
        this.auditLogService = auditLogService;
        this.storageRoot = Path.of(storageDir).toAbsolutePath().normalize();
    }

    @Override
    @Transactional
    public AttachmentResponseDTO createAttachment(CreateAttachmentRequest request) {
        Issue issue = issueRepository.findById(request.getRelatedEntityId())
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));

        Attachment attachment = new Attachment();
        attachment.setIssue(issue);
        attachment.setFileName(request.getFileName());
        attachment.setOriginalFilename(request.getFileName());
        attachment.setFileType(request.getFileType());
        attachment.setFileSize(request.getFileData() == null ? null : (long) request.getFileData().length);
        attachment.setFileUrl("path/to/storage/" + request.getFileName());
        attachment.setProcessingStatus(AttachmentProcessingStatus.COMPLETED);
        attachment.setProcessedAt(LocalDateTime.now());

        return convertToResponseDTO(attachmentRepository.save(attachment));
    }

    @Override
    @Transactional
    public AttachmentResponseDTO uploadAttachment(Long issueId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Upload file must not be empty.");
        }

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));

        Attachment attachment = new Attachment();
        attachment.setIssue(issue);
        attachment.setUploadedBy(resolveUploader());
        attachment.setFileName(cleanFilename(file.getOriginalFilename()));
        attachment.setOriginalFilename(cleanFilename(file.getOriginalFilename()));
        attachment.setFileType(file.getContentType());
        attachment.setFileSize(file.getSize());
        attachment.setProcessingStatus(AttachmentProcessingStatus.PENDING);

        Attachment saved = attachmentRepository.save(attachment);

        String storedFilename = buildStoredFilename(saved.getId(), saved.getOriginalFilename());
        Path pendingPath = pendingDirectory().resolve(storedFilename).normalize();

        try {
            Files.createDirectories(pendingPath.getParent());
            Files.copy(file.getInputStream(), pendingPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            saved.setProcessingStatus(AttachmentProcessingStatus.FAILED);
            saved.setProcessingError("Could not save uploaded file for processing.");
            saved.setProcessedAt(LocalDateTime.now());
            attachmentRepository.save(saved);
            throw new BadRequestException("Could not save uploaded file.");
        }

        saved.setStoredFilename(storedFilename);
        saved.setFileUrl(pendingPath.toString());
        saved.setProcessingStatus(AttachmentProcessingStatus.PROCESSING);
        Attachment queued = attachmentRepository.save(saved);

        scheduleProcessingAfterCommit(queued.getId(), pendingPath.toString());
        auditLogService.logCurrentUser("ATTACHMENT_UPLOADED", "ATTACHMENT", queued.getId());

        return convertToResponseDTO(queued);
    }

    private void scheduleProcessingAfterCommit(Long attachmentId, String pendingPath) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    attachmentProcessingService.processAttachmentAsync(attachmentId, pendingPath);
                }
            });
            return;
        }

        attachmentProcessingService.processAttachmentAsync(attachmentId, pendingPath);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentResponseDTO> getAllAttachments() {
        return attachmentRepository.findAllByOrderByUploadedAtDesc()
                .stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AttachmentResponseDTO getAttachmentById(Long id) {
        return convertToResponseDTO(findAttachment(id));
    }

    @Override
    @Transactional(readOnly = true)
    public AttachmentResponseDTO getAttachmentStatus(Long id) {
        return convertToResponseDTO(findAttachment(id));
    }

    @Override
    @Transactional
    public void deleteAttachment(Long id) {
        Attachment attachment = findAttachment(id);
        deleteIfExists(attachment.getFileUrl());
        deleteIfExists(attachment.getThumbnailPath());
        attachmentRepository.delete(attachment);
        auditLogService.logCurrentUser("ATTACHMENT_DELETED", "ATTACHMENT", id);
    }

    private Attachment findAttachment(Long id) {
        return attachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found"));
    }

    private User resolveUploader() {
        Long userId = TenantContext.getUserId();
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId).orElse(null);
    }

    private Path pendingDirectory() {
        return tenantDirectory().resolve("pending");
    }

    private Path tenantDirectory() {
        String tenant = TenantContext.getIdentifier();
        if (!StringUtils.hasText(tenant)) {
            tenant = TenantContext.getSchemaName();
        }
        return storageRoot.resolve(safePathSegment(tenant)).normalize();
    }

    private String buildStoredFilename(Long attachmentId, String originalFilename) {
        String extension = "";
        String filename = cleanFilename(originalFilename);
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < filename.length() - 1) {
            extension = filename.substring(dotIndex).replaceAll("[^A-Za-z0-9.]", "");
        }
        return attachmentId + "-" + UUID.randomUUID() + extension;
    }

    private String cleanFilename(String filename) {
        String cleaned = StringUtils.cleanPath(filename == null ? "upload" : filename);
        if (!StringUtils.hasText(cleaned) || cleaned.contains("..")) {
            return "upload";
        }
        return cleaned;
    }

    private String safePathSegment(String value) {
        return StringUtils.hasText(value) ? value.replaceAll("[^A-Za-z0-9._-]", "_") : "default";
    }

    private void deleteIfExists(String pathValue) {
        if (!StringUtils.hasText(pathValue)) {
            return;
        }

        try {
            Path path = Path.of(pathValue).toAbsolutePath().normalize();
            if (path.startsWith(storageRoot)) {
                Files.deleteIfExists(path);
            }
        } catch (IOException ignored) {
            // Deleting the database record should not be blocked by best-effort file cleanup.
        }
    }

    private AttachmentResponseDTO convertToResponseDTO(Attachment attachment) {
        AttachmentResponseDTO dto = new AttachmentResponseDTO();
        dto.setId(attachment.getId());
        dto.setRelatedEntityId(attachment.getIssue().getId());
        dto.setUploadedById(attachment.getUploadedBy() == null ? null : attachment.getUploadedBy().getId());
        dto.setFileName(attachment.getFileName());
        dto.setFileType(attachment.getFileType());
        dto.setFileSize(attachment.getFileSize());
        dto.setFileUrl(attachment.getFileUrl());
        dto.setOriginalFilename(attachment.getOriginalFilename());
        dto.setStoredFilename(attachment.getStoredFilename());
        dto.setThumbnailPath(attachment.getThumbnailPath());
        dto.setProcessingStatus(attachment.getProcessingStatus() == null ? null : attachment.getProcessingStatus().name());
        dto.setProcessingError(attachment.getProcessingError());
        dto.setUploadedAt(attachment.getUploadedAt() == null ? null : attachment.getUploadedAt().toString());
        dto.setProcessedAt(attachment.getProcessedAt() == null ? null : attachment.getProcessedAt().toString());
        return dto;
    }
}
