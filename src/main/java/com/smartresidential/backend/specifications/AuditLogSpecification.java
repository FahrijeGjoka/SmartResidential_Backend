package com.smartresidential.backend.specifications;

import com.smartresidential.backend.dto.auditLog.AuditLogFilterRequest;
import com.smartresidential.backend.entities.AuditLog;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

public class AuditLogSpecification {

    public static Specification<AuditLog> withFilters(
            AuditLogFilterRequest filter
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
            // 🔥 ACTION FILTER
            // =========================
            if (filter.getAction() != null && !filter.getAction().isEmpty()) {
                predicates.add(
                        cb.equal(root.get("action"), filter.getAction())
                );
            }

            // =========================
            // 🔥 ENTITY TYPE FILTER
            // =========================
            if (filter.getEntityType() != null && !filter.getEntityType().isEmpty()) {
                predicates.add(
                        cb.equal(root.get("entityType"), filter.getEntityType())
                );
            }

            // =========================
            // 🔥 ENTITY ID FILTER
            // =========================
            if (filter.getEntityId() != null) {
                predicates.add(
                        cb.equal(root.get("entityId"), filter.getEntityId())
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