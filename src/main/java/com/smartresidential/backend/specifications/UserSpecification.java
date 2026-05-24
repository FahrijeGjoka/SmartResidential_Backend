package com.smartresidential.backend.specifications;

import com.smartresidential.backend.dto.user.UserFilterRequest;
import com.smartresidential.backend.entities.User;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

public class UserSpecification {

    public static Specification<User> withFilters(
            UserFilterRequest filter
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filter.getRoleId() != null) {
                predicates.add(
                        cb.equal(root.get("roleId"), filter.getRoleId())
                );
            }

            if (filter.getEmail() != null && !filter.getEmail().isEmpty()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("email")),
                                "%" + filter.getEmail().toLowerCase() + "%"
                        )
                );
            }

            if (filter.getFirstName() != null && !filter.getFirstName().isEmpty()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("firstName")),
                                "%" + filter.getFirstName().toLowerCase() + "%"
                        )
                );
            }

            if (filter.getLastName() != null && !filter.getLastName().isEmpty()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("lastName")),
                                "%" + filter.getLastName().toLowerCase() + "%"
                        )
                );
            }

            if (filter.getIsActive() != null) {
                predicates.add(
                        cb.equal(root.get("isActive"), filter.getIsActive())
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