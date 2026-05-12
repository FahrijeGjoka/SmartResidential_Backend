package com.smartresidential.backend.dto.maintenanceRequest;

import com.smartresidential.backend.dto.common.BaseFilterRequest;
import java.time.LocalDateTime;

public class MaintenanceRequestFilterRequest extends BaseFilterRequest {

    private Long issueId;

    private Long requestedByUserId;

    private String description;

    private LocalDateTime requestedAfter;

    private LocalDateTime requestedBefore;

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public Long getRequestedByUserId() {
        return requestedByUserId;
    }

    public void setRequestedByUserId(Long requestedByUserId) {
        this.requestedByUserId = requestedByUserId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getRequestedAfter() {
        return requestedAfter;
    }

    public void setRequestedAfter(LocalDateTime requestedAfter) {
        this.requestedAfter = requestedAfter;
    }

    public LocalDateTime getRequestedBefore() {
        return requestedBefore;
    }

    public void setRequestedBefore(LocalDateTime requestedBefore) {
        this.requestedBefore = requestedBefore;
    }
}