package com.smartresidential.backend.dto.maintenanceRequest;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public class CreateMaintenanceRequestRequest {

    @NotNull(message = "Issue ID is required")
    private Long issueId;

    @NotNull(message = "User ID is required")
    private Long requestedById;

    private String requestNote;

    // Getters and Setters
    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public Long getRequestedById() {
        return requestedById;
    }

    public void setRequestedById(Long requestedById) {
        this.requestedById = requestedById;
    }

    public String getRequestNote() {
        return requestNote;
    }

    public void setRequestNote(String requestNote) {
        this.requestNote = requestNote;
    }
}