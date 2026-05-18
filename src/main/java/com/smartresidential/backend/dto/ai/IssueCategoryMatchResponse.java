package com.smartresidential.backend.dto.ai;

public class IssueCategoryMatchResponse {

    private Long categoryId;
    private String categoryName;
    private Double confidence;
    private String reason;

    public IssueCategoryMatchResponse() {
    }

    public IssueCategoryMatchResponse(Long categoryId, String categoryName, Double confidence, String reason) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.confidence = confidence;
        this.reason = reason;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
