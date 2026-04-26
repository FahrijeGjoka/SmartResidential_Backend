package com.smartresidential.backend.dto.issueStatusHistory;

public class CreateIssueStatusHistoryRequest {

    private String newStatus;
    private Long changedByUserId;

    public CreateIssueStatusHistoryRequest() {
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public Long getChangedByUserId() {
        return changedByUserId;
    }

    public void setChangedByUserId(Long changedByUserId) {
        this.changedByUserId = changedByUserId;
    }
}