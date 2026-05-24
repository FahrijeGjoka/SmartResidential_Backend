package com.smartresidential.backend.specifications;

import com.smartresidential.backend.dto.workLog.WorkLogFilterRequest;
import com.smartresidential.backend.entities.WorkLog;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

public class WorkLogSpecification {

    public static Specification<WorkLog> withFilters(
            WorkLogFilterRequest filter
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filter.getIssueId() != null) {
                predicates.add(
                        cb.equal(root.get("issue").get("id"), filter.getIssueId())
                );
            }

            if (filter.getTechnicianId() != null) {
                predicates.add(
                        cb.equal(root.get("technician").get("id"), filter.getTechnicianId())
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

            if (filter.getMinHoursSpent() != null && filter.getMaxHoursSpent() != null) {
                predicates.add(
                        cb.between(root.get("hoursSpent"),
                                filter.getMinHoursSpent(),
                                filter.getMaxHoursSpent())
                );
            } else if (filter.getMinHoursSpent() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("hoursSpent"), filter.getMinHoursSpent())
                );
            } else if (filter.getMaxHoursSpent() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("hoursSpent"), filter.getMaxHoursSpent())
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