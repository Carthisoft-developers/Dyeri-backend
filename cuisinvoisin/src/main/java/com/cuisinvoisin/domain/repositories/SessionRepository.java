// com/cuisinvoisin/domain/repositories/SessionRepository.java
package com.cuisinvoisin.domain.repositories;

import com.cuisinvoisin.domain.entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {
    Optional<Session> findByRefreshTokenHash(String hash);
    void deleteAllByUser_Id(UUID userId);
    void deleteByRefreshTokenHash(String hash);
}
