package com.smartresidential.backend.dto.dashboard;

import java.time.LocalDateTime;

public class ResidentDashboardAnnouncement {

    private Long id;
    private String title;
    private LocalDateTime createdAt;

    public ResidentDashboardAnnouncement() {
    }

    public ResidentDashboardAnnouncement(Long id, String title, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
