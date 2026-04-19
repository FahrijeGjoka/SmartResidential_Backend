package com.smartresidential.backend.dto.issueAssignment;

public class CreateIssueAssignmentRequest {

    private Long issueId;
    private Long technicianId;

    public CreateIssueAssignmentRequest() {
    }

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
}