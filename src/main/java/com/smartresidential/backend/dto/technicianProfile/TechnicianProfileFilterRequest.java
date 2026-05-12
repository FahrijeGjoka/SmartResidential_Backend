package com.smartresidential.backend.dto.technicianProfile;

import com.smartresidential.backend.dto.common.BaseFilterRequest;

public class TechnicianProfileFilterRequest extends BaseFilterRequest {

    private Long userId;

    private String specialization;

    private Boolean isAvailable;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }
}