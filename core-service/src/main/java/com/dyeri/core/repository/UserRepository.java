package com.dyeri.core.domain.repositories;

import com.dyeri.core.domain.entities.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserRepository extends R2dbcRepository<User, UUID> {
    Mono<User> findByEmail(String email);
    Mono<Boolean> existsByEmail(String email);
    Flux<User> findByRole(String role);
}
