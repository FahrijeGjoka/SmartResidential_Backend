package com.smartresidential.backend.dto.dashboard;

import java.time.LocalDateTime;

public class ResidentDashboardIssue {

    private Long id;
    private String title;
    private String status;
    private LocalDateTime updatedAt;

    public ResidentDashboardIssue() {
    }

    public ResidentDashboardIssue(Long id, String title, String status, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
