package com.smartresidential.backend.dto.residentprofile;

import com.smartresidential.backend.dto.common.BaseFilterRequest;
import java.time.LocalDateTime;

public class ResidentProfileFilterRequest extends BaseFilterRequest {

    private Long userId;

    private Long apartmentId;

    private LocalDateTime movedInAfter;

    private LocalDateTime movedInBefore;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getApartmentId() {
        return apartmentId;
    }

    public void setApartmentId(Long apartmentId) {
        this.apartmentId = apartmentId;
    }

    public LocalDateTime getMovedInAfter() {
        return movedInAfter;
    }

    public void setMovedInAfter(LocalDateTime movedInAfter) {
        this.movedInAfter = movedInAfter;
    }

    public LocalDateTime getMovedInBefore() {
        return movedInBefore;
    }

    public void setMovedInBefore(LocalDateTime movedInBefore) {
        this.movedInBefore = movedInBefore;
    }
}