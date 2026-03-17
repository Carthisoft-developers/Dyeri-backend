// com/cuisinvoisin/domain/repositories/CookRepository.java
package com.cuisinvoisin.domain.repositories;

import com.cuisinvoisin.domain.entities.Cook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface CookRepository extends JpaRepository<Cook, UUID> {

    Page<Cook> findByIsAvailableTrue(Pageable pageable);

    @Query("""
        SELECT c FROM Cook c
        WHERE c.isAvailable = true
          AND (6371 * acos(
                cos(radians(:lat)) * cos(radians(c.latitude)) *
                cos(radians(c.longitude) - radians(:lng)) +
                sin(radians(:lat)) * sin(radians(c.latitude))
              )) <= :radiusKm
        ORDER BY (6371 * acos(
                cos(radians(:lat)) * cos(radians(c.latitude)) *
                cos(radians(c.longitude) - radians(:lng)) +
                sin(radians(:lat)) * sin(radians(c.latitude))
              )) ASC
        """)
    Page<Cook> findNearbyCooks(@Param("lat") double lat,
                               @Param("lng") double lng,
                               @Param("radiusKm") int radiusKm,
                               Pageable pageable);
}
