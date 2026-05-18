package com.smartresidential.backend.ai;

import com.smartresidential.backend.entities.IssueCategory;

public class IssueCategoryMatch {

    private final IssueCategory category;
    private final double confidence;
    private final String reason;

    public IssueCategoryMatch(IssueCategory category, double confidence, String reason) {
        this.category = category;
        this.confidence = confidence;
        this.reason = reason;
    }

    public IssueCategory getCategory() {
        return category;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getReason() {
        return reason;
    }
}
