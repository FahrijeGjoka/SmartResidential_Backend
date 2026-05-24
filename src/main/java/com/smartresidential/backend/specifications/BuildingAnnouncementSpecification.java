package com.smartresidential.backend.specifications;

import com.smartresidential.backend.dto.buildingAnnouncement.BuildingAnnouncementFilterRequest;
import com.smartresidential.backend.entities.BuildingAnnouncement;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

public class BuildingAnnouncementSpecification {

    public static Specification<BuildingAnnouncement> withFilters(
            BuildingAnnouncementFilterRequest filter
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filter.getBuildingId() != null) {
                predicates.add(
                        cb.equal(root.get("building").get("id"), filter.getBuildingId())
                );
            }

            if (filter.getCreatedByUserId() != null) {
                predicates.add(
                        cb.equal(root.get("createdBy").get("id"), filter.getCreatedByUserId())
                );
            }

            if (filter.getTitle() != null && !filter.getTitle().isEmpty()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("title")),
                                "%" + filter.getTitle().toLowerCase() + "%"
                        )
                );
            }

            if (filter.getMessage() != null && !filter.getMessage().isEmpty()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("message")),
                                "%" + filter.getMessage().toLowerCase() + "%"
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

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}