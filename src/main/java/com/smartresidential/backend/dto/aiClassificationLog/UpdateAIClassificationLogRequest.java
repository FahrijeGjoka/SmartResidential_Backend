package com.smartresidential.backend.dto.aiClassificationLog;

public class UpdateAIClassificationLogRequest {

    private String predictedCategory;
    private String predictedPriority;
    private Double confidenceScore;

    public UpdateAIClassificationLogRequest() {
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
}