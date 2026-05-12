package com.smartresidential.backend.specifications;

import com.smartresidential.backend.dto.issue.IssueFilterRequest;
import com.smartresidential.backend.entities.Issue;
import com.smartresidential.backend.entities.IssueAssignment;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class IssueSpecification {

    public static Specification<Issue> withFilters(
            IssueFilterRequest filter
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            if (filter.getCreatedById() != null) {
                predicates.add(
                        cb.equal(root.get("createdBy").get("id"), filter.getCreatedById())
                );
            }

            if (filter.getApartmentId() != null) {
                predicates.add(
                        cb.equal(root.get("apartment").get("id"), filter.getApartmentId())
                );
            }

            if (filter.getCategoryId() != null) {
                predicates.add(
                        cb.equal(root.get("category").get("id"), filter.getCategoryId())
                );
            }

            if (filter.getAssignedTechnicianId() != null) {
                Subquery<Long> assignmentSubquery = query.subquery(Long.class);
                Root<IssueAssignment> assignmentRoot = assignmentSubquery.from(IssueAssignment.class);

                assignmentSubquery.select(assignmentRoot.get("issue").get("id"))
                        .where(
                                cb.equal(assignmentRoot.get("issue").get("id"), root.get("id")),
                                cb.equal(assignmentRoot.get("technician").get("id"), filter.getAssignedTechnicianId())
                        );

                predicates.add(cb.exists(assignmentSubquery));
            }

            if (hasText(filter.getStatus())) {
                predicates.add(
                        cb.equal(root.get("status"), filter.getStatus())
                );
            }

            if (hasText(filter.getPriority())) {
                predicates.add(
                        cb.equal(root.get("priority"), filter.getPriority())
                );
            }

            if (hasText(filter.getKeyword())) {
                String keyword = "%" + filter.getKeyword().trim().toLowerCase() + "%";
                predicates.add(
                        cb.or(
                                cb.like(cb.lower(root.get("title")), keyword),
                                cb.like(cb.lower(root.get("description")), keyword)
                        )
                );
            }

            if (hasText(filter.getTitle())) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("title")),
                                "%" + filter.getTitle().trim().toLowerCase() + "%"
                        )
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

            if (filter.getUpdatedAfter() != null && filter.getUpdatedBefore() != null) {
                predicates.add(
                        cb.between(root.get("updatedAt"),
                                filter.getUpdatedAfter(),
                                filter.getUpdatedBefore())
                );
            } else if (filter.getUpdatedAfter() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("updatedAt"), filter.getUpdatedAfter())
                );
            } else if (filter.getUpdatedBefore() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("updatedAt"), filter.getUpdatedBefore())
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
