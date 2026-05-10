package com.smartresidential.backend.dto.workLog;

import com.smartresidential.backend.dto.common.BaseFilterRequest;
import java.time.LocalDateTime;

public class WorkLogFilterRequest extends BaseFilterRequest {

    private Long issueId;

    private Long technicianId;

    private String description;

    private Double minHoursSpent;

    private Double maxHoursSpent;

    private LocalDateTime createdAfter;

    private LocalDateTime createdBefore;

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public Long getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Long technicianId) {
        this.technicianId = technicianId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getMinHoursSpent() {
        return minHoursSpent;
    }

    public void setMinHoursSpent(Double minHoursSpent) {
        this.minHoursSpent = minHoursSpent;
    }

    public Double getMaxHoursSpent() {
        return maxHoursSpent;
    }

    public void setMaxHoursSpent(Double maxHoursSpent) {
        this.maxHoursSpent = maxHoursSpent;
    }

    public LocalDateTime getCreatedAfter() {
        return createdAfter;
    }

    public void setCreatedAfter(LocalDateTime createdAfter) {
        this.createdAfter = createdAfter;
    }

    public LocalDateTime getCreatedBefore() {
        return createdBefore;
    }

    public void setCreatedBefore(LocalDateTime createdBefore) {
        this.createdBefore = createdBefore;
    }
}