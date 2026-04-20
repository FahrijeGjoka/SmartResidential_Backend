package com.smartresidential.backend.dto.technicianProfile;

public class UpdateTechnicianProfileRequest {

    private String specialization;
    private Boolean isAvailable;

    public UpdateTechnicianProfileRequest() {
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