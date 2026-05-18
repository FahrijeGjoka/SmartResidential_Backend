package com.smartresidential.backend.dto.ai;

public class IssueCategoryMatchRequest {

    private String title;
    private String description;

    public IssueCategoryMatchRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
