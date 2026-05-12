package com.smartresidential.backend.dto.aiClassificationLog;

import com.smartresidential.backend.dto.common.BaseFilterRequest;
import java.time.LocalDateTime;

public class AIClassificationLogFilterRequest extends BaseFilterRequest {

    private Long issueId;

    private String predictedCategory;

    private String predictedPriority;

    private Double minConfidenceScore;

    private Double maxConfidenceScore;

    private LocalDateTime createdAfter;

    private LocalDateTime createdBefore;

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
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

    public Double getMinConfidenceScore() {
        return minConfidenceScore;
    }

    public void setMinConfidenceScore(Double minConfidenceScore) {
        this.minConfidenceScore = minConfidenceScore;
    }

    public Double getMaxConfidenceScore() {
        return maxConfidenceScore;
    }

    public void setMaxConfidenceScore(Double maxConfidenceScore) {
        this.maxConfidenceScore = maxConfidenceScore;
    }

    public LocalDateTime getCreatedAfter() {
        return createdAfter;
    }

    public void setCreatedAfter(LocalDateTime createdAfter) {
        this.createdAfter = createdAfter;
    }

    public LocalDateTime getCreatedBefore() {
        return createdBefore;
    }

    public void setCreatedBefore(LocalDateTime createdBefore) {
        this.createdBefore = createdBefore;
    }
}