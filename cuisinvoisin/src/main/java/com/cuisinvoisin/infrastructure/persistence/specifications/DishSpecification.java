// com/cuisinvoisin/infrastructure/persistence/specifications/DishSpecification.java
package com.cuisinvoisin.infrastructure.persistence.specifications;

import com.cuisinvoisin.application.bean.request.DishFilterRequest;
import com.cuisinvoisin.domain.entities.Dish;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class DishSpecification {

    private DishSpecification() {}

    public static Specification<Dish> from(DishFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.cookId() != null) {
                predicates.add(cb.equal(root.get("cook").get("id"), filter.cookId()));
            }
            if (filter.categoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filter.categoryId()));
            }
            if (filter.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.minPrice()));
            }
            if (filter.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.maxPrice()));
            }
            if (filter.available() != null) {
                predicates.add(cb.equal(root.get("available"), filter.available()));
            }
            if (filter.query() != null && !filter.query().isBlank()) {
                String pattern = "%" + filter.query().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
