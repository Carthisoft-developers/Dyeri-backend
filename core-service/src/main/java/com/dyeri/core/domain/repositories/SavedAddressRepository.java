package com.dyeri.core.domain.repositories;

import com.dyeri.core.domain.entities.SavedAddress;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface SavedAddressRepository extends R2dbcRepository<SavedAddress, UUID> {
    Flux<SavedAddress> findByClientIdOrderByDefaultAddressDescCreatedAtDesc(UUID clientId);

    @Modifying
    @Query("UPDATE saved_addresses SET is_default = false WHERE client_id = :clientId")
    Mono<Void> clearDefaultsByClientId(UUID clientId);
}
