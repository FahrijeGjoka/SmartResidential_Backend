package com.smartresidential.backend.dto.buildingAnnouncement;

public class CreateBuildingAnnouncementRequest {

    private Long buildingId;
    private String title;
    private String message;
    private Long createdBy;

    public CreateBuildingAnnouncementRequest() {
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
}
