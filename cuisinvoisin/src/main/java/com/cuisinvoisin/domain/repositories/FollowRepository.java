// com/cuisinvoisin/domain/repositories/FollowRepository.java
package com.cuisinvoisin.domain.repositories;

import com.cuisinvoisin.domain.entities.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, Follow.FollowPK> {
    List<Follow> findByClientIdOrderByFollowedAtDesc(UUID clientId);
    boolean existsByClientIdAndCookId(UUID clientId, UUID cookId);
    void deleteByClientIdAndCookId(UUID clientId, UUID cookId);
}
