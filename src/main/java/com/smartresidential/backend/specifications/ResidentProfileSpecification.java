package com.smartresidential.backend.specifications;

import com.smartresidential.backend.dto.residentprofile.ResidentProfileFilterRequest;
import com.smartresidential.backend.entities.ResidentProfile;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

public class ResidentProfileSpecification {

    public static Specification<ResidentProfile> withFilters(
            ResidentProfileFilterRequest filter
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // =========================
            // 🔥 USER FILTER
            // =========================
            if (filter.getUserId() != null) {
                predicates.add(
                        cb.equal(root.get("user").get("id"), filter.getUserId())
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
            // 🔥 MOVED IN RANGE
            // =========================
            if (filter.getMovedInAfter() != null && filter.getMovedInBefore() != null) {
                predicates.add(
                        cb.between(root.get("movedInAt"),
                                filter.getMovedInAfter(),
                                filter.getMovedInBefore())
                );
            } else if (filter.getMovedInAfter() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("movedInAt"), filter.getMovedInAfter())
                );
            } else if (filter.getMovedInBefore() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("movedInAt"), filter.getMovedInBefore())
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}