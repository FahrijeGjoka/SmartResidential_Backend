package com.smartresidential.backend.dto.issueStatusHistory;

public class CreateIssueStatusHistoryRequest {

    private String newStatus;

    public CreateIssueStatusHistoryRequest() {
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }
}