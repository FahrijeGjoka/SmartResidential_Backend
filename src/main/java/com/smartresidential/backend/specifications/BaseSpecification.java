package com.smartresidential.backend.specifications;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BaseSpecification<T> {

    private final List<Specification<T>> specs = new ArrayList<>();

    public BaseSpecification<T> add(Specification<T> spec) {
        if (spec != null) {
            specs.add(spec);
        }
        return this;
    }

    public Specification<T> build() {
        return specs.stream()
                .reduce(Specification::and)
                .orElse(null);
    }

    // =========================
    // 🔥 REUSABLE HELPERS
    // =========================

    public static <T> Specification<T> equalsSpec(String field, Object value) {
        return (root, query, cb) -> {
            if (value == null) return null;
            return cb.equal(root.get(field), value);
        };
    }

    public static <T> Specification<T> likeSpec(String field, String value) {
        return (root, query, cb) -> {
            if (value == null || value.isEmpty()) return null;
            return cb.like(
                    cb.lower(root.get(field)),
                    "%" + value.toLowerCase() + "%"
            );
        };
    }

    public static <T> Specification<T> inSpec(String field, List<?> values) {
        return (root, query, cb) -> {
            if (values == null || values.isEmpty()) return null;
            return root.get(field).in(values);
        };
    }

    public static <T> Specification<T> greaterThanOrEqual(String field, Comparable value) {
        return (root, query, cb) -> {
            if (value == null) return null;
            return cb.greaterThanOrEqualTo(root.get(field), value);
        };
    }

    public static <T> Specification<T> lessThanOrEqual(String field, Comparable value) {
        return (root, query, cb) -> {
            if (value == null) return null;
            return cb.lessThanOrEqualTo(root.get(field), value);
        };
    }

    // =========================
    // 🔥 DATE RANGE HELPER
    // =========================

    public static <T> Specification<T> betweenDates(
            String field,
            LocalDateTime start,
            LocalDateTime end
    ) {
        return (root, query, cb) -> {
            if (start == null && end == null) return null;

            if (start != null && end != null) {
                return cb.between(root.get(field), start, end);
            }

            if (start != null) {
                return cb.greaterThanOrEqualTo(root.get(field), start);
            }

            return cb.lessThanOrEqualTo(root.get(field), end);
        };
    }

    // =========================
    // 🔥 JOIN HELPER
    // =========================

    public static <T> Specification<T> joinEquals(
            String joinField,
            String attribute,
            Object value
    ) {
        return (root, query, cb) -> {
            if (value == null) return null;

            return cb.equal(
                    root.join(joinField).get(attribute),
                    value
            );
        };
    }

    // =========================
    // 🔥 SEARCH MULTI FIELD
    // =========================

    public static <T> Specification<T> multiLike(List<String> fields, String value) {
        return (root, query, cb) -> {
            if (value == null || value.isEmpty()) return null;

            List<Predicate> predicates = new ArrayList<>();

            for (String field : fields) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get(field)),
                                "%" + value.toLowerCase() + "%"
                        )
                );
            }

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}