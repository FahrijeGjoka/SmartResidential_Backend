package com.smartresidential.backend.dto.maintenanceRequest;

import jakarta.validation.constraints.NotNull;

public class CreateMaintenanceRequestRequest {

    @NotNull(message = "Issue ID is required")
    private Long issueId;

    @NotNull(message = "User ID is required")
    private Long requestedById;

    private String description;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}