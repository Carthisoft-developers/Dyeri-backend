package com.cuisinvoisin.backend.modules.auth.infrastructure.persistence;

import com.cuisinvoisin.backend.modules.auth.domain.entities.Session;
import com.cuisinvoisin.backend.modules.auth.domain.repositories.SessionRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface JpaSessionRepository extends JpaRepository<Session, UUID>, SessionRepository {
    List<Session> findByUserId(UUID userId);
}
