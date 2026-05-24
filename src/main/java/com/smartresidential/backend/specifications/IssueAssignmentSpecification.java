package com.smartresidential.backend.specifications;

import com.smartresidential.backend.dto.issueAssignment.IssueAssignmentFilterRequest;
import com.smartresidential.backend.entities.IssueAssignment;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

public class IssueAssignmentSpecification {

    public static Specification<IssueAssignment> withFilters(
            IssueAssignmentFilterRequest filter
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

            if (filter.getAssignedAfter() != null && filter.getAssignedBefore() != null) {
                predicates.add(
                        cb.between(root.get("assignedAt"),
                                filter.getAssignedAfter(),
                                filter.getAssignedBefore())
                );
            } else if (filter.getAssignedAfter() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("assignedAt"), filter.getAssignedAfter())
                );
            } else if (filter.getAssignedBefore() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("assignedAt"), filter.getAssignedBefore())
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}