package com.smartresidential.backend.specifications;

import com.smartresidential.backend.dto.maintenanceRequest.MaintenanceRequestFilterRequest;
import com.smartresidential.backend.entities.MaintenanceRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

public class MaintenanceRequestSpecification {

    public static Specification<MaintenanceRequest> withFilters(
            MaintenanceRequestFilterRequest filter
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filter.getIssueId() != null) {
                predicates.add(
                        cb.equal(root.get("issue").get("id"), filter.getIssueId())
                );
            }

            if (filter.getRequestedByUserId() != null) {
                predicates.add(
                        cb.equal(root.get("requestedBy").get("id"), filter.getRequestedByUserId())
                );
            }

            if (filter.getDescription() != null && !filter.getDescription().isEmpty()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("description")),
                                "%" + filter.getDescription().toLowerCase() + "%"
                        )
                );
            }

            if (filter.getRequestedAfter() != null && filter.getRequestedBefore() != null) {
                predicates.add(
                        cb.between(root.get("requestedAt"),
                                filter.getRequestedAfter(),
                                filter.getRequestedBefore())
                );
            } else if (filter.getRequestedAfter() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("requestedAt"), filter.getRequestedAfter())
                );
            } else if (filter.getRequestedBefore() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("requestedAt"), filter.getRequestedBefore())
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}