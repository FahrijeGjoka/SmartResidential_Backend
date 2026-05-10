package com.smartresidential.backend.specifications;

import com.smartresidential.backend.dto.apartment.ApartmentFilterRequest;
import com.smartresidential.backend.entities.Apartment;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

public class ApartmentSpecification {

    public static Specification<Apartment> withFilters(
            ApartmentFilterRequest filter
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // =========================
            // 🔥 BUILDING FILTER
            // =========================
            if (filter.getBuildingId() != null) {
                predicates.add(
                        cb.equal(root.get("building").get("id"), filter.getBuildingId())
                );
            }

            // =========================
            // 🔥 UNIT NUMBER SEARCH
            // =========================
            if (filter.getUnitNumber() != null && !filter.getUnitNumber().isEmpty()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("unitNumber")),
                                "%" + filter.getUnitNumber().toLowerCase() + "%"
                        )
                );
            }

            // =========================
            // 🔥 FLOOR FILTER
            // =========================
            if (filter.getFloor() != null) {
                predicates.add(
                        cb.equal(root.get("floor"), filter.getFloor())
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