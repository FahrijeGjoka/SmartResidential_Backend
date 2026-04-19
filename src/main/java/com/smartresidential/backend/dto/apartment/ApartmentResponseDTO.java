package com.smartresidential.backend.dto.apartment;

import java.time.LocalDateTime;

public class ApartmentResponseDTO {

    private Long id;
    private Long buildingId;
    private String unitNumber;
    private Integer floor;
    private LocalDateTime createdAt;

    public ApartmentResponseDTO() {
    }

    public ApartmentResponseDTO(Long id, Long buildingId, String unitNumber, Integer floor, LocalDateTime createdAt) {
        this.id = id;
        this.buildingId = buildingId;
        this.unitNumber = unitNumber;
        this.floor = floor;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
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

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}