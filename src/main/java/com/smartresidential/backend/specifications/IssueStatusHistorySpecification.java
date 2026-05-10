package com.smartresidential.backend.specifications;

import com.smartresidential.backend.dto.issueStatusHistory.IssueStatusHistoryFilterRequest;
import com.smartresidential.backend.entities.IssueStatusHistory;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

public class IssueStatusHistorySpecification {

    public static Specification<IssueStatusHistory> withFilters(
            IssueStatusHistoryFilterRequest filter
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // =========================
            // 🔥 ISSUE FILTER
            // =========================
            if (filter.getIssueId() != null) {
                predicates.add(
                        cb.equal(root.get("issue").get("id"), filter.getIssueId())
                );
            }

            // =========================
            // 🔥 CHANGED BY USER
            // =========================
            if (filter.getChangedByUserId() != null) {
                predicates.add(
                        cb.equal(root.get("changedBy").get("id"), filter.getChangedByUserId())
                );
            }

            // =========================
            // 🔥 OLD STATUS
            // =========================
            if (filter.getOldStatus() != null && !filter.getOldStatus().isEmpty()) {
                predicates.add(
                        cb.equal(root.get("oldStatus"), filter.getOldStatus())
                );
            }

            // =========================
            // 🔥 NEW STATUS
            // =========================
            if (filter.getNewStatus() != null && !filter.getNewStatus().isEmpty()) {
                predicates.add(
                        cb.equal(root.get("newStatus"), filter.getNewStatus())
                );
            }

            // =========================
            // 🔥 CHANGED AT RANGE
            // =========================
            if (filter.getChangedAfter() != null && filter.getChangedBefore() != null) {
                predicates.add(
                        cb.between(root.get("changedAt"),
                                filter.getChangedAfter(),
                                filter.getChangedBefore())
                );
            } else if (filter.getChangedAfter() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("changedAt"), filter.getChangedAfter())
                );
            } else if (filter.getChangedBefore() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("changedAt"), filter.getChangedBefore())
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}