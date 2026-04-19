package com.smartresidential.backend.dto.buildingAnnouncement;

import java.time.LocalDateTime;

public class BuildingAnnouncementResponseDTO {

    private Long id;
    private Long buildingId;
    private String title;
    private String message;
    private Long createdBy;
    private LocalDateTime createdAt;

    public BuildingAnnouncementResponseDTO() {
    }

    public BuildingAnnouncementResponseDTO(Long id, Long buildingId, String title, String message, Long createdBy, LocalDateTime createdAt) {
        this.id = id;
        this.buildingId = buildingId;
        this.title = title;
        this.message = message;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getBuildingId() {
        return buildingId;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setBuildingId(Long buildingId) {
        this.buildingId = buildingId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}