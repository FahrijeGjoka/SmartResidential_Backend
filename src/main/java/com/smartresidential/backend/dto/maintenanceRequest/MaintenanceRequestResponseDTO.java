package com.smartresidential.backend.dto.maintenanceRequest;

import java.time.LocalDateTime;

public class MaintenanceRequestResponseDTO {

    private Long id;
    private Long issueId;
    private String issueTitle;
    private String issueStatus;
    private String issuePriority;
    private Long assignedTechnicianUserId;
    private String assignedTechnicianName;
    private String workOrderStatus;
    private Long requestedById;
    private String description;
    private LocalDateTime requestedAt;

    public MaintenanceRequestResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public String getIssueTitle() {
        return issueTitle;
    }

    public void setIssueTitle(String issueTitle) {
        this.issueTitle = issueTitle;
    }

    public String getIssueStatus() {
        return issueStatus;
    }

    public void setIssueStatus(String issueStatus) {
        this.issueStatus = issueStatus;
    }

    public String getIssuePriority() {
        return issuePriority;
    }

    public void setIssuePriority(String issuePriority) {
        this.issuePriority = issuePriority;
    }

    public Long getAssignedTechnicianUserId() {
        return assignedTechnicianUserId;
    }

    public void setAssignedTechnicianUserId(Long assignedTechnicianUserId) {
        this.assignedTechnicianUserId = assignedTechnicianUserId;
    }

    public String getAssignedTechnicianName() {
        return assignedTechnicianName;
    }

    public void setAssignedTechnicianName(String assignedTechnicianName) {
        this.assignedTechnicianName = assignedTechnicianName;
    }

    public String getWorkOrderStatus() {
        return workOrderStatus;
    }

    public void setWorkOrderStatus(String workOrderStatus) {
        this.workOrderStatus = workOrderStatus;
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

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }
}
