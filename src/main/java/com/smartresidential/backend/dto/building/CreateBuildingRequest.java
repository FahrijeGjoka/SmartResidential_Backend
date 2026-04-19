package com.smartresidential.backend.dto.building;

public class CreateBuildingRequest {

    private String name;
    private String address;

    public CreateBuildingRequest() {
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