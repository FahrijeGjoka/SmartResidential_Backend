package com.smartresidential.backend.dto.residentProfile;

import java.time.LocalDateTime;

public class ResidentProfileResponseDTO {

    private Long id;
    private Long userId;
    private Long apartmentId;
    private LocalDateTime movedInAt;

    public ResidentProfileResponseDTO() {
    }

    public ResidentProfileResponseDTO(Long id, Long userId, Long apartmentId, LocalDateTime movedInAt) {
        this.id = id;
        this.userId = userId;
        this.apartmentId = apartmentId;
        this.movedInAt = movedInAt;
    }

    public Long getId() {
        return id;
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

    public void setId(Long id) {
        this.id = id;
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