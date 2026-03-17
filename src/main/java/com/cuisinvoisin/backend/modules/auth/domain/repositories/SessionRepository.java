package com.cuisinvoisin.backend.modules.auth.domain.repositories;

import com.cuisinvoisin.backend.modules.auth.domain.entities.Session;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface SessionRepository {
    Optional<Session> findById(UUID id);
    List<Session> findByUserId(UUID userId);
    Session save(Session session);
    void deleteById(UUID id);
}
