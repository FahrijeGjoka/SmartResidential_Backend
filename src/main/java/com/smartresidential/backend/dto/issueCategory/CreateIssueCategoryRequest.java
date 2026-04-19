package com.smartresidential.backend.dto.issueCategory;

public class CreateIssueCategoryRequest {

    private String name;
    private String description;

    public CreateIssueCategoryRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}