package com.smartresidential.backend.dto.issueAssignment;

import com.smartresidential.backend.dto.common.BaseFilterRequest;
import java.time.LocalDateTime;

public class IssueAssignmentFilterRequest extends BaseFilterRequest {

    private Long issueId;

    private Long technicianId;

    private LocalDateTime assignedAfter;

    private LocalDateTime assignedBefore;

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

    public LocalDateTime getAssignedAfter() {
        return assignedAfter;
    }

    public void setAssignedAfter(LocalDateTime assignedAfter) {
        this.assignedAfter = assignedAfter;
    }

    public LocalDateTime getAssignedBefore() {
        return assignedBefore;
    }

    public void setAssignedBefore(LocalDateTime assignedBefore) {
        this.assignedBefore = assignedBefore;
    }
}