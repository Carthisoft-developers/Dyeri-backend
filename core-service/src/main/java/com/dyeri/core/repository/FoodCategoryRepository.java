package com.dyeri.core.domain.repositories;

import com.dyeri.core.domain.entities.FoodCategory;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import java.util.UUID;

public interface FoodCategoryRepository extends R2dbcRepository<FoodCategory, UUID> {}
