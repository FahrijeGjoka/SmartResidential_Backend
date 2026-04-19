package com.smartresidential.backend.dto.residentProfile;

import java.time.LocalDateTime;

public class CreateResidentProfileRequest {

    private Long userId;
    private Long apartmentId;
    private LocalDateTime movedInAt;

    public CreateResidentProfileRequest() {
    }

    public Long getUserId() {
        return userId;
    }

    public Long getApartmentId() {
        return apartmentId;
    }

    public LocalDateTime getMovedInAt() {
        return movedInAt;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setApartmentId(Long apartmentId) {
        this.apartmentId = apartmentId;
    }

    public void setMovedInAt(LocalDateTime movedInAt) {
        this.movedInAt = movedInAt;
    }
}