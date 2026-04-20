package com.smartresidential.backend.dto.auditLog;

public class UpdateAuditLogRequest {

    private String action;

    public UpdateAuditLogRequest() {
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}