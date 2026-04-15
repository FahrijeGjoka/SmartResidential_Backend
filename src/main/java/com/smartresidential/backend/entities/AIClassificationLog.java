package com.smartresidential.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_classification_logs")
public class AIClassificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    @Column(name = "raw_input", columnDefinition = "TEXT")
    private String rawInput;

    @Column(name = "predicted_category")
    private String predictedCategory;

    @Column(name = "predicted_priority")
    private String predictedPriority;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public AIClassificationLog() {
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Issue getIssue() {
        return issue;
    }

    public String getRawInput() {
        return rawInput;
    }

    public String getPredictedCategory() {
        return predictedCategory;
    }

    public String getPredictedPriority() {
        return predictedPriority;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setIssue(Issue issue) {
        this.issue = issue;
    }

    public void setRawInput(String rawInput) {
        this.rawInput = rawInput;
    }

    public void setPredictedCategory(String predictedCategory) {
        this.predictedCategory = predictedCategory;
    }

    public void setPredictedPriority(String predictedPriority) {
        this.predictedPriority = predictedPriority;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

