package com.dyeri.core.interfaces.rest.handlers;

import com.dyeri.core.domain.services.FavoriteService;
import com.dyeri.core.domain.services.FollowService;
import com.dyeri.core.infrastructure.security.SecurityContextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SocialHandler {

    private final FavoriteService favoriteService;
    private final FollowService followService;

    public Mono<ServerResponse> getMyFavorites(ServerRequest req) {
        return SecurityContextUtils.getCurrentUserId()
                .flatMapMany(favoriteService::getFavorites)
                .collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    public Mono<ServerResponse> addFavorite(ServerRequest req) {
        UUID dishId = UUID.fromString(req.pathVariable("dishId"));
        return SecurityContextUtils.getCurrentUserId()
                .flatMap(uid -> favoriteService.addFavorite(uid, dishId))
                .then(ServerResponse.status(HttpStatus.CREATED).build());
    }

    public Mono<ServerResponse> removeFavorite(ServerRequest req) {
        UUID dishId = UUID.fromString(req.pathVariable("dishId"));
        return SecurityContextUtils.getCurrentUserId()
                .flatMap(uid -> favoriteService.removeFavorite(uid, dishId))
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> getMyFollowing(ServerRequest req) {
        return SecurityContextUtils.getCurrentUserId()
                .flatMapMany(followService::getFollowedCooks)
                .collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    public Mono<ServerResponse> followCook(ServerRequest req) {
        UUID cookId = UUID.fromString(req.pathVariable("cookId"));
        return SecurityContextUtils.getCurrentUserId()
                .flatMap(uid -> followService.follow(uid, cookId))
                .then(ServerResponse.status(HttpStatus.CREATED).build());
    }

    public Mono<ServerResponse> unfollowCook(ServerRequest req) {
        UUID cookId = UUID.fromString(req.pathVariable("cookId"));
        return SecurityContextUtils.getCurrentUserId()
                .flatMap(uid -> followService.unfollow(uid, cookId))
                .then(ServerResponse.noContent().build());
    }
}
