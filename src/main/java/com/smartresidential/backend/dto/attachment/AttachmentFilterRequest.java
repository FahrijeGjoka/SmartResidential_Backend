package com.smartresidential.backend.dto.attachment;

import com.smartresidential.backend.dto.common.BaseFilterRequest;
import java.time.LocalDateTime;

public class AttachmentFilterRequest extends BaseFilterRequest {

    private Long issueId;

    private String fileName;

    private LocalDateTime uploadedAfter;

    private LocalDateTime uploadedBefore;

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public LocalDateTime getUploadedAfter() {
        return uploadedAfter;
    }

    public void setUploadedAfter(LocalDateTime uploadedAfter) {
        this.uploadedAfter = uploadedAfter;
    }

    public LocalDateTime getUploadedBefore() {
        return uploadedBefore;
    }

    public void setUploadedBefore(LocalDateTime uploadedBefore) {
        this.uploadedBefore = uploadedBefore;
    }
}