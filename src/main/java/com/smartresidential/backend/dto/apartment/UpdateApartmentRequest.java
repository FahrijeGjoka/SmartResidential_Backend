package com.smartresidential.backend.dto.apartment;

public class UpdateApartmentRequest {

    private Long buildingId;
    private String unitNumber;
    private Integer floor;

    public UpdateApartmentRequest() {
    }

    public Long getBuildingId() {
        return buildingId;
    }

    public String getUnitNumber() {
        return unitNumber;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setBuildingId(Long buildingId) {
        this.buildingId = buildingId;
    }

    public void setUnitNumber(String unitNumber) {
        this.unitNumber = unitNumber;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }
}