package com.smartresidential.backend.dto.building;

public class UpdateBuildingRequest {

    private String name;
    private String address;

    public UpdateBuildingRequest() {
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}