// com/dyeri/core/interfaces/rest/handlers/CookHandler.java
package com.dyeri.core.interfaces.rest.handlers;

import com.dyeri.core.application.bean.request.UpdateCookProfileRequest;
import com.dyeri.core.domain.exceptions.UnauthorizedException;
import com.dyeri.core.domain.services.CookService;
import com.dyeri.core.domain.services.ReviewService;
import com.dyeri.core.infrastructure.security.SecurityContextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CookHandler {
    private final CookService cookService;
    private final ReviewService reviewService;

    public Mono<ServerResponse> getNearbyCooks(ServerRequest req) {
        double lat = Double.parseDouble(req.queryParam("lat").orElse("0"));
        double lng = Double.parseDouble(req.queryParam("lng").orElse("0"));
        int radius = Integer.parseInt(req.queryParam("radius").orElse("10"));
        return cookService.getNearbyCooks(lat, lng, radius).collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    public Mono<ServerResponse> getCook(ServerRequest req) {
        return cookService.getCookProfile(UUID.fromString(req.pathVariable("id")))
                .flatMap(r -> ServerResponse.ok().bodyValue(r));
    }

    public Mono<ServerResponse> getCookReviews(ServerRequest req) {
        UUID id = UUID.fromString(req.pathVariable("id"));
        int page = Integer.parseInt(req.queryParam("page").orElse("0"));
        int size = Integer.parseInt(req.queryParam("size").orElse("20"));
        return reviewService.getCookReviews(id, page, size).collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    public Mono<ServerResponse> getMyProfile(ServerRequest req) {
        return SecurityContextUtils.getCurrentUserId()
                .switchIfEmpty(Mono.error(new UnauthorizedException("Authentication required")))
                .flatMap(cookService::getCookProfile)
                .flatMap(r -> ServerResponse.ok().bodyValue(r));
    }

    public Mono<ServerResponse> updateMyProfile(ServerRequest req) {
        return SecurityContextUtils.getCurrentUserId()
                .switchIfEmpty(Mono.error(new UnauthorizedException("Authentication required")))
                .flatMap(uid -> req.bodyToMono(UpdateCookProfileRequest.class)
                        .flatMap(body -> cookService.updateCookProfile(uid, body)))
                .flatMap(r -> ServerResponse.ok().bodyValue(r));
    }

    public Mono<ServerResponse> getDashboard(ServerRequest req) {
        return SecurityContextUtils.getCurrentUserId()
                .switchIfEmpty(Mono.error(new UnauthorizedException("Authentication required")))
                .flatMap(cookService::getCookDashboard)
                .flatMap(r -> ServerResponse.ok().bodyValue(r));
    }
}
