package com.smartresidential.backend.dto.notification;

public class UpdateNotificationRequest {

    private Boolean isRead;

    public UpdateNotificationRequest() {
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
}