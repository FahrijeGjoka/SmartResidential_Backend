package com.smartresidential.backend.specifications;

import com.smartresidential.backend.dto.technicianProfile.TechnicianProfileFilterRequest;
import com.smartresidential.backend.entities.TechnicianProfile;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

public class TechnicianProfileSpecification {

    public static Specification<TechnicianProfile> withFilters(
            TechnicianProfileFilterRequest filter
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filter.getUserId() != null) {
                predicates.add(
                        cb.equal(root.get("user").get("id"), filter.getUserId())
                );
            }

            if (filter.getSpecialization() != null && !filter.getSpecialization().isEmpty()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("specialization")),
                                "%" + filter.getSpecialization().toLowerCase() + "%"
                        )
                );
            }

            if (filter.getIsAvailable() != null) {
                predicates.add(
                        cb.equal(root.get("isAvailable"), filter.getIsAvailable())
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}