package com.smartresidential.backend.dto.technicianProfile;

public class UpdateTechnicianProfileRequest {

    private String specialization;
    private Boolean isAvailable;
    private Integer maxActiveIssues;

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

    public Integer getMaxActiveIssues() {
        return maxActiveIssues;
    }

    public void setMaxActiveIssues(Integer maxActiveIssues) {
        this.maxActiveIssues = maxActiveIssues;
    }
}
