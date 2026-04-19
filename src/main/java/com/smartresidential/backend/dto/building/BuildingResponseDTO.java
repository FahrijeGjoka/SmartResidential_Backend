package com.smartresidential.backend.dto.building;

import java.time.LocalDateTime;

public class BuildingResponseDTO {

    private Long id;
    private String name;
    private String address;
    private LocalDateTime createdAt;

    public BuildingResponseDTO() {
    }

    public BuildingResponseDTO(Long id, String name, String address, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}