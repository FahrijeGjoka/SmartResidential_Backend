package com.smartresidential.backend.dto.issueAssignment;

public class CreateIssueAssignmentRequest {

    private Long technicianId;

    public CreateIssueAssignmentRequest() {
    }

    public Long getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Long technicianId) {
        this.technicianId = technicianId;
    }
}