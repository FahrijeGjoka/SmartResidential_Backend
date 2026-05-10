package com.smartresidential.backend.specifications;

import com.smartresidential.backend.dto.issue.IssueFilterRequest;
import com.smartresidential.backend.entities.Issue;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

public class IssueSpecification {

    public static Specification<Issue> withFilters(
            IssueFilterRequest filter
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // =========================
            // 🔥 CREATED BY USER
            // =========================
            if (filter.getCreatedByUserId() != null) {
                predicates.add(
                        cb.equal(root.get("createdBy").get("id"), filter.getCreatedByUserId())
                );
            }

            // =========================
            // 🔥 APARTMENT FILTER
            // =========================
            if (filter.getApartmentId() != null) {
                predicates.add(
                        cb.equal(root.get("apartment").get("id"), filter.getApartmentId())
                );
            }

            // =========================
            // 🔥 CATEGORY FILTER
            // =========================
            if (filter.getCategoryId() != null) {
                predicates.add(
                        cb.equal(root.get("category").get("id"), filter.getCategoryId())
                );
            }

            // =========================
            // 🔥 STATUS FILTER
            // =========================
            if (filter.getStatus() != null && !filter.getStatus().isEmpty()) {
                predicates.add(
                        cb.equal(root.get("status"), filter.getStatus())
                );
            }

            // =========================
            // 🔥 PRIORITY FILTER
            // =========================
            if (filter.getPriority() != null && !filter.getPriority().isEmpty()) {
                predicates.add(
                        cb.equal(root.get("priority"), filter.getPriority())
                );
            }

            // =========================
            // 🔥 TITLE SEARCH
            // =========================
            if (filter.getTitle() != null && !filter.getTitle().isEmpty()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("title")),
                                "%" + filter.getTitle().toLowerCase() + "%"
                        )
                );
            }

            // =========================
            // 🔥 CREATED AT RANGE
            // =========================
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

            // =========================
            // 🔥 UPDATED AT RANGE
            // =========================
            if (filter.getUpdatedAfter() != null && filter.getUpdatedBefore() != null) {
                predicates.add(
                        cb.between(root.get("updatedAt"),
                                filter.getUpdatedAfter(),
                                filter.getUpdatedBefore())
                );
            } else if (filter.getUpdatedAfter() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("updatedAt"), filter.getUpdatedAfter())
                );
            } else if (filter.getUpdatedBefore() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("updatedAt"), filter.getUpdatedBefore())
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}