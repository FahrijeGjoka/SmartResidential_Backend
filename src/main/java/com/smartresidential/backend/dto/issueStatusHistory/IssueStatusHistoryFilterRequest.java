package com.smartresidential.backend.dto.issueStatusHistory;

import com.smartresidential.backend.dto.common.BaseFilterRequest;
import java.time.LocalDateTime;

public class IssueStatusHistoryFilterRequest extends BaseFilterRequest {

    private Long issueId;

    private Long changedByUserId;

    private String oldStatus;

    private String newStatus;

    private LocalDateTime changedAfter;

    private LocalDateTime changedBefore;

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public Long getChangedByUserId() {
        return changedByUserId;
    }

    public void setChangedByUserId(Long changedByUserId) {
        this.changedByUserId = changedByUserId;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public LocalDateTime getChangedAfter() {
        return changedAfter;
    }

    public void setChangedAfter(LocalDateTime changedAfter) {
        this.changedAfter = changedAfter;
    }

    public LocalDateTime getChangedBefore() {
        return changedBefore;
    }

    public void setChangedBefore(LocalDateTime changedBefore) {
        this.changedBefore = changedBefore;
    }
}