package com.dyeri.core.domain.services;
import com.dyeri.core.application.bean.response.CookSummaryResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

/** Inbound port for client-cook follow relationships. */
public interface FollowService {
    Mono<Void> follow(UUID clientId, UUID cookId);
    Mono<Void> unfollow(UUID clientId, UUID cookId);
    Mono<Void> toggleFollow(UUID clientId, UUID cookId);
    Flux<CookSummaryResponse> getFollowedCooks(UUID clientId);
}
