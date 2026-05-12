package com.smartresidential.backend.specifications;

import com.smartresidential.backend.dto.comment.CommentFilterRequest;
import com.smartresidential.backend.entities.Comment;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

public class CommentSpecification {

    public static Specification<Comment> withFilters(
            CommentFilterRequest filter
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // =========================
            // 🔥 ISSUE FILTER (CORE)
            // =========================
            if (filter.getIssueId() != null) {
                predicates.add(
                        cb.equal(root.get("issue").get("id"), filter.getIssueId())
                );
            }

            // =========================
            // 🔥 USER FILTER
            // =========================
            if (filter.getUserId() != null) {
                predicates.add(
                        cb.equal(root.get("user").get("id"), filter.getUserId())
                );
            }

            // =========================
            // 🔥 CONTENT SEARCH
            // =========================
            if (filter.getContent() != null && !filter.getContent().isEmpty()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("content")),
                                "%" + filter.getContent().toLowerCase() + "%"
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

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}