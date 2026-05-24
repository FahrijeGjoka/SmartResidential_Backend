package com.smartresidential.backend.specifications;

import com.smartresidential.backend.dto.attachment.AttachmentFilterRequest;
import com.smartresidential.backend.entities.Attachment;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

public class AttachmentSpecification {

    public static Specification<Attachment> withFilters(
            AttachmentFilterRequest filter
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filter.getIssueId() != null) {
                predicates.add(
                        cb.equal(root.get("issue").get("id"), filter.getIssueId())
                );
            }

            if (filter.getFileName() != null && !filter.getFileName().isEmpty()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("fileName")),
                                "%" + filter.getFileName().toLowerCase() + "%"
                        )
                );
            }

            if (filter.getUploadedAfter() != null && filter.getUploadedBefore() != null) {
                predicates.add(
                        cb.between(root.get("uploadedAt"),
                                filter.getUploadedAfter(),
                                filter.getUploadedBefore())
                );
            } else if (filter.getUploadedAfter() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("uploadedAt"), filter.getUploadedAfter())
                );
            } else if (filter.getUploadedBefore() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("uploadedAt"), filter.getUploadedBefore())
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}