package com.smartresidential.backend.specifications;

import com.smartresidential.backend.dto.issueCategory.IssueCategoryFilterRequest;
import com.smartresidential.backend.entities.IssueCategory;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

public class IssueCategorySpecification {

    public static Specification<IssueCategory> withFilters(
            IssueCategoryFilterRequest filter
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // =========================
            // 🔥 NAME SEARCH
            // =========================
            if (filter.getName() != null && !filter.getName().isEmpty()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("name")),
                                "%" + filter.getName().toLowerCase() + "%"
                        )
                );
            }

            // =========================
            // 🔥 DESCRIPTION SEARCH
            // =========================
            if (filter.getDescription() != null && !filter.getDescription().isEmpty()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("description")),
                                "%" + filter.getDescription().toLowerCase() + "%"
                        )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}