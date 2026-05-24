package com.smartresidential.backend.specifications;

import com.smartresidential.backend.dto.aiClassificationLog.AIClassificationLogFilterRequest;
import com.smartresidential.backend.entities.AIClassificationLog;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

public class AIClassificationLogSpecification {

    public static Specification<AIClassificationLog> withFilters(
            AIClassificationLogFilterRequest filter
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filter.getIssueId() != null) {
                predicates.add(
                        cb.equal(root.get("issue").get("id"), filter.getIssueId())
                );
            }

            if (filter.getPredictedCategory() != null && !filter.getPredictedCategory().isEmpty()) {
                predicates.add(
                        cb.equal(root.get("predictedCategory"), filter.getPredictedCategory())
                );
            }

            if (filter.getPredictedPriority() != null && !filter.getPredictedPriority().isEmpty()) {
                predicates.add(
                        cb.equal(root.get("predictedPriority"), filter.getPredictedPriority())
                );
            }

            if (filter.getMinConfidenceScore() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("confidenceScore"), filter.getMinConfidenceScore())
                );
            }

            if (filter.getMaxConfidenceScore() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("confidenceScore"), filter.getMaxConfidenceScore())
                );
            }

            if (filter.getCreatedAfter() != null && filter.getCreatedBefore() != null) {
                predicates.add(
                        cb.between(root.get("createdAt"),
                                filter.getCreatedAfter(),
                                filter.getCreatedBefore())
                );
            } else if (filter.getCreatedAfter() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedAfter())
                );
            } else if (filter.getCreatedBefore() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedBefore())
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}