package com.smartresidential.backend.dto.technicianProfile;

import java.time.LocalDateTime;

public class TechnicianProfileResponseDTO {

    private Long id;
    private Long userId;
    private String specialization;
    private Boolean isAvailable;
    private Integer activeIssueCount;
    private Integer activeHighPriorityIssueCount;
    private Integer maxActiveIssues;
    private LocalDateTime lastAssignedAt;

    public TechnicianProfileResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public Integer getActiveIssueCount() {
        return activeIssueCount;
    }

    public void setActiveIssueCount(Integer activeIssueCount) {
        this.activeIssueCount = activeIssueCount;
    }

    public Integer getActiveHighPriorityIssueCount() {
        return activeHighPriorityIssueCount;
    }

    public void setActiveHighPriorityIssueCount(Integer activeHighPriorityIssueCount) {
        this.activeHighPriorityIssueCount = activeHighPriorityIssueCount;
    }

    public Integer getMaxActiveIssues() {
        return maxActiveIssues;
    }

    public void setMaxActiveIssues(Integer maxActiveIssues) {
        this.maxActiveIssues = maxActiveIssues;
    }

    public LocalDateTime getLastAssignedAt() {
        return lastAssignedAt;
    }

    public void setLastAssignedAt(LocalDateTime lastAssignedAt) {
        this.lastAssignedAt = lastAssignedAt;
    }
}
