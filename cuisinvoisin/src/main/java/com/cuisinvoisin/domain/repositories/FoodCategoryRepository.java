// com/cuisinvoisin/domain/repositories/FoodCategoryRepository.java
package com.cuisinvoisin.domain.repositories;

import com.cuisinvoisin.domain.entities.FoodCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FoodCategoryRepository extends JpaRepository<FoodCategory, UUID> {}
