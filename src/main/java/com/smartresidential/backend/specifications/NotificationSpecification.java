package com.smartresidential.backend.specifications;

import com.smartresidential.backend.dto.notification.NotificationFilterRequest;
import com.smartresidential.backend.entities.Notification;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

public class NotificationSpecification {

    public static Specification<Notification> withFilters(
            NotificationFilterRequest filter
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
            // 🔥 TYPE FILTER
            // =========================
            if (filter.getType() != null && !filter.getType().isEmpty()) {
                predicates.add(
                        cb.equal(root.get("type"), filter.getType())
                );
            }

            // =========================
            // 🔥 READ / UNREAD FILTER
            // =========================
            if (filter.getIsRead() != null) {
                predicates.add(
                        cb.equal(root.get("isRead"), filter.getIsRead())
                );
            }

            // =========================
            // 🔥 MESSAGE SEARCH
            // =========================
            if (filter.getMessage() != null && !filter.getMessage().isEmpty()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("message")),
                                "%" + filter.getMessage().toLowerCase() + "%"
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