// com/dyeri/core/application/services/FollowServiceImpl.java
package com.dyeri.core.application.services;

import com.dyeri.core.application.bean.response.CookSummaryResponse;
import com.dyeri.core.domain.entities.Follow;
import com.dyeri.core.domain.repositories.*;
import com.dyeri.core.domain.services.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

        @Override
        public Mono<Void> follow(UUID clientId, UUID cookId) {
                return followRepository.existsByClientIdAndCookId(clientId, cookId)
                                .flatMap(exists -> exists
                                                ? Mono.empty()
                                                : followRepository.save(Follow.builder()
                                                                .clientId(clientId)
                                                                .cookId(cookId)
                                                                .followedAt(Instant.now())
                                                                .build()).then());
        }

        @Override
        public Mono<Void> unfollow(UUID clientId, UUID cookId) {
                return followRepository.deleteByClientIdAndCookId(clientId, cookId);
        }

    @Override
    public Mono<Void> toggleFollow(UUID clientId, UUID cookId) {
        return followRepository.existsByClientIdAndCookId(clientId, cookId)
                .flatMap(exists -> exists
                        ? followRepository.deleteByClientIdAndCookId(clientId, cookId)
                        : followRepository.save(Follow.builder()
                                .clientId(clientId).cookId(cookId).followedAt(Instant.now()).build()).then());
    }

    @Override
    public Flux<CookSummaryResponse> getFollowedCooks(UUID clientId) {
        return followRepository.findByClientIdOrderByFollowedAtDesc(clientId)
                .flatMap(f -> userRepository.findById(f.getCookId()))
                .map(u -> new CookSummaryResponse(u.getId(), u.getName(), u.getAvatar(),
                        u.getRating() != null ? u.getRating() : 0.0,
                        u.getReviewCount() != null ? u.getReviewCount() : 0,
                        Boolean.TRUE.equals(u.getAvailable()), 0.0));
    }
}
