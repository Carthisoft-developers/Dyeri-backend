// com/cuisinvoisin/domain/repositories/DishOptionGroupRepository.java
package com.cuisinvoisin.domain.repositories;

import com.cuisinvoisin.domain.entities.DishOptionGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DishOptionGroupRepository extends JpaRepository<DishOptionGroup, UUID> {}
