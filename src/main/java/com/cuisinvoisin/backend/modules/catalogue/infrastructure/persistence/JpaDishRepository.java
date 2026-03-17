package com.cuisinvoisin.backend.modules.catalogue.infrastructure.persistence;

import com.cuisinvoisin.backend.modules.catalogue.domain.entities.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface JpaDishRepository extends JpaRepository<Dish, UUID> {
    List<Dish> findByCookId(UUID cookId);
}
