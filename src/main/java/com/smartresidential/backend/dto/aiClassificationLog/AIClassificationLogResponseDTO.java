package com.smartresidential.backend.dto.aiClassificationLog;

public class AIClassificationLogResponseDTO {

    private Long id;
    private Long issueId;
    private String rawInput;
    private String predictedCategory;
    private String predictedPriority;
    private Double confidenceScore;
    private String createdAt;

    public AIClassificationLogResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public String getRawInput() {
        return rawInput;
    }

    public void setRawInput(String rawInput) {
        this.rawInput = rawInput;
    }

    public String getPredictedCategory() {
        return predictedCategory;
    }

    public void setPredictedCategory(String predictedCategory) {
        this.predictedCategory = predictedCategory;
    }

    public String getPredictedPriority() {
        return predictedPriority;
    }

    public void setPredictedPriority(String predictedPriority) {
        this.predictedPriority = predictedPriority;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}