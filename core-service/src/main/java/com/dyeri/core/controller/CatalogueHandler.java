// com/dyeri/core/interfaces/rest/handlers/CatalogueHandler.java
package com.dyeri.core.interfaces.rest.handlers;

import com.dyeri.core.application.bean.request.*;
import com.dyeri.core.domain.exceptions.UnauthorizedException;
import com.dyeri.core.domain.services.CatalogueService;
import com.dyeri.core.infrastructure.security.SecurityContextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CatalogueHandler {
    private final CatalogueService catalogueService;

    public Mono<ServerResponse> getCategories(ServerRequest req) {
        return catalogueService.getAllCategories().collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    public Mono<ServerResponse> createCategory(ServerRequest req) {
        String name = req.queryParam("name").orElseThrow();
        String icon = req.queryParam("icon").orElse("");
        return catalogueService.createCategory(name, icon)
                .flatMap(r -> ServerResponse.status(HttpStatus.CREATED).bodyValue(r));
    }

    public Mono<ServerResponse> getDishes(ServerRequest req) {
        int page = Integer.parseInt(req.queryParam("page").orElse("0"));
        int size = Integer.parseInt(req.queryParam("size").orElse("20"));
        var filter = new DishFilterRequest(
                req.queryParam("cookId").map(UUID::fromString).orElse(null),
                req.queryParam("categoryId").map(UUID::fromString).orElse(null),
                req.queryParam("minPrice").map(BigDecimal::new).orElse(null),
                req.queryParam("maxPrice").map(BigDecimal::new).orElse(null),
                req.queryParam("available").map(Boolean::parseBoolean).orElse(null),
                req.queryParam("query").orElse(null));
        return catalogueService.getDishes(filter, page, size).collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    public Mono<ServerResponse> getDish(ServerRequest req) {
        return catalogueService.getDish(UUID.fromString(req.pathVariable("id")))
                .flatMap(r -> ServerResponse.ok().bodyValue(r));
    }

    public Mono<ServerResponse> createDish(ServerRequest req) {
        return SecurityContextUtils.getCurrentUserId()
                .flatMap(uid -> req.bodyToMono(CreateDishRequest.class)
                        .flatMap(body -> catalogueService.createDish(uid, body)))
                .flatMap(r -> ServerResponse.status(HttpStatus.CREATED).bodyValue(r));
    }

    public Mono<ServerResponse> updateDish(ServerRequest req) {
        UUID id = UUID.fromString(req.pathVariable("id"));
        return SecurityContextUtils.getCurrentUserId()
                .flatMap(uid -> req.bodyToMono(UpdateDishRequest.class)
                        .flatMap(body -> catalogueService.updateDish(uid, id, body)))
                .flatMap(r -> ServerResponse.ok().bodyValue(r));
    }

    public Mono<ServerResponse> uploadDishImage(ServerRequest req) {
        UUID id = UUID.fromString(req.pathVariable("id"));
        return SecurityContextUtils.getCurrentUserId()
                .switchIfEmpty(Mono.error(new UnauthorizedException("Authentication required")))
                .flatMap(uid -> req.multipartData()
                        .map(parts -> (FilePart) parts.getFirst("file"))
                        .flatMap(file -> catalogueService.uploadDishImage(uid, id, file)))
                .flatMap(r -> ServerResponse.ok().bodyValue(r));
    }

    public Mono<ServerResponse> getDishImage(ServerRequest req) {
        UUID id = UUID.fromString(req.pathVariable("id"));
        return catalogueService.getDishImage(id)
                .flatMap(bytes -> ServerResponse.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .bodyValue(bytes));
    }

    public Mono<ServerResponse> deleteDish(ServerRequest req) {
        UUID id = UUID.fromString(req.pathVariable("id"));
        return SecurityContextUtils.getCurrentUserId()
                .flatMap(uid -> catalogueService.deleteDish(uid, id))
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> toggleAvailability(ServerRequest req) {
        UUID id = UUID.fromString(req.pathVariable("id"));
        return SecurityContextUtils.getCurrentUserId()
                .flatMap(uid -> catalogueService.toggleAvailability(uid, id))
                .flatMap(r -> ServerResponse.ok().bodyValue(r));
    }
}
