package com.smartresidential.backend.dto.maintenancerequest;

import java.time.LocalDateTime;

public class CreateMaintenanceRequestRequest {
    private Long userId;
    private String issueDescription;
    private LocalDateTime requestedDate;
    private String priority;

    // Getter dhe Setter
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getIssueDescription() {
        return issueDescription;
    }

    public void setIssueDescription(String issueDescription) {
        this.issueDescription = issueDescription;
    }

    public LocalDateTime getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(LocalDateTime requestedDate) {
        this.requestedDate = requestedDate;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}