package com.cuisinvoisin.backend.modules.users.infrastructure.persistence;

import com.cuisinvoisin.backend.modules.users.domain.entities.User;
import com.cuisinvoisin.backend.modules.users.domain.repositories.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaUserRepository extends JpaRepository<User, UUID>, UserRepository {
    Optional<User> findByEmail(String email);
}
