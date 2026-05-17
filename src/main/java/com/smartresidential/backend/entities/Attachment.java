package com.smartresidential.backend.entities;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attachments")
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    @ManyToOne
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "file_name")
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false)
    private AttachmentProcessingStatus processingStatus = AttachmentProcessingStatus.PENDING;

    @Column(name = "processing_error", columnDefinition = "TEXT")
    private String processingError;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "original_filename")
    private String originalFilename;

    @Column(name = "stored_filename")
    private String storedFilename;

    @Column(name = "thumbnail_path")
    private String thumbnailPath;

    public Attachment() {
    }

    @PrePersist
    public void prePersist() {
        if (this.uploadedAt == null) {
            this.uploadedAt = LocalDateTime.now();
        }
        if (this.processingStatus == null) {
            this.processingStatus = AttachmentProcessingStatus.PENDING;
        }
    }

    public Long getId() {
        return id;
    }

    public Issue getIssue() {
        return issue;
    }

    public User getUploadedBy() {
        return uploadedBy;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public AttachmentProcessingStatus getProcessingStatus() {
        return processingStatus;
    }

    public String getProcessingError() {
        return processingError;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public String getFileType() {
        return fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getStoredFilename() {
        return storedFilename;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setIssue(Issue issue) {
        this.issue = issue;
    }

    public void setUploadedBy(User uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setProcessingStatus(AttachmentProcessingStatus processingStatus) {
        this.processingStatus = processingStatus;
    }

    public void setProcessingError(String processingError) {
        this.processingError = processingError;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public void setStoredFilename(String storedFilename) {
        this.storedFilename = storedFilename;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }
}
