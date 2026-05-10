package com.smartresidential.backend.specifications;

import com.smartresidential.backend.dto.building.BuildingFilterRequest;
import com.smartresidential.backend.entities.Building;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

public class BuildingSpecification {

    public static Specification<Building> withFilters(
            BuildingFilterRequest filter
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
            // 🔥 ADDRESS SEARCH
            // =========================
            if (filter.getAddress() != null && !filter.getAddress().isEmpty()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("address")),
                                "%" + filter.getAddress().toLowerCase() + "%"
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