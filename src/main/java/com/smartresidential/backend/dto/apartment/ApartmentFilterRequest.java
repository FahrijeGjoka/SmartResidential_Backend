package com.smartresidential.backend.dto.apartment;

import com.smartresidential.backend.dto.common.BaseFilterRequest;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class ApartmentFilterRequest extends BaseFilterRequest {

    private Long buildingId;

    private String unitNumber;

    private Integer floor;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdAfter;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdBefore;

    public Long getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Long buildingId) {
        this.buildingId = buildingId;
    }

    public String getUnitNumber() {
        return unitNumber;
    }

    public void setUnitNumber(String unitNumber) {
        this.unitNumber = unitNumber;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public LocalDateTime getCreatedAfter() {
        return createdAfter;
    }

    public void setCreatedAfter(LocalDateTime createdAfter) {
        this.createdAfter = createdAfter;
    }

    public LocalDateTime getCreatedBefore() {
        return createdBefore;
    }

    public void setCreatedBefore(LocalDateTime createdBefore) {
        this.createdBefore = createdBefore;
    }
}
