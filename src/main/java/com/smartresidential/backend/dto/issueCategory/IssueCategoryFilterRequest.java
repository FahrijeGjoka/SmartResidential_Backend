package com.smartresidential.backend.dto.issueCategory;

import com.smartresidential.backend.dto.common.BaseFilterRequest;

public class IssueCategoryFilterRequest extends BaseFilterRequest {

    private String name;

    private String description;

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