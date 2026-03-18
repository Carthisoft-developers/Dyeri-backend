package com.dyeri.payment.repository;
import com.dyeri.payment.entity.CookPayout;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import java.util.UUID;
public interface CookPayoutRepository extends R2dbcRepository<CookPayout, UUID> {
    Flux<CookPayout> findByCookId(UUID cookId);
}