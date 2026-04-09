package com.dyeri.core.interfaces.rest.handlers;

import com.dyeri.core.application.bean.request.LocationUpdateRequest;
import com.dyeri.core.domain.exceptions.UnauthorizedException;
import com.dyeri.core.domain.services.DeliveryService;
import com.dyeri.core.infrastructure.security.SecurityContextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeliveryHandler {
    private final DeliveryService deliveryService;

    public Mono<ServerResponse> getAvailableOrders(ServerRequest req) {
        return requireDeliveryUserId()
                .flatMapMany(deliveryService::getAvailableOrders)
                .collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    public Mono<ServerResponse> getHistory(ServerRequest req) {
        int page = Integer.parseInt(req.queryParam("page").orElse("0"));
        int size = Integer.parseInt(req.queryParam("size").orElse("100"));
        return requireDeliveryUserId()
                .flatMapMany(uid -> deliveryService.getHistory(uid, page, size))
                .collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    public Mono<ServerResponse> getEarnings(ServerRequest req) {
        return requireDeliveryUserId()
                .flatMap(deliveryService::getEarnings)
                .flatMap(res -> ServerResponse.ok().bodyValue(res));
    }

    public Mono<ServerResponse> acceptDelivery(ServerRequest req) {
        UUID orderId = UUID.fromString(req.pathVariable("orderId"));
        return requireDeliveryUserId()
                .flatMap(uid -> deliveryService.acceptDelivery(uid, orderId))
                .flatMap(res -> ServerResponse.ok().bodyValue(res));
    }

    public Mono<ServerResponse> completeDelivery(ServerRequest req) {
        UUID orderId = UUID.fromString(req.pathVariable("orderId"));
        return requireDeliveryUserId()
                .flatMap(uid -> readProofPhoto(req)
                        .flatMap(optProof -> deliveryService.completeDelivery(uid, orderId, optProof.orElse(null))))
                .flatMap(res -> ServerResponse.ok().bodyValue(res));
    }

    public Mono<ServerResponse> updateLocation(ServerRequest req) {
        UUID orderId = UUID.fromString(req.pathVariable("orderId"));
        return requireDeliveryUserId()
                .flatMap(uid -> req.bodyToMono(LocationUpdateRequest.class)
                        .flatMap(body -> deliveryService.updateDriverLocation(uid, orderId, body)))
                .then(ServerResponse.noContent().build());
    }

    private Mono<UUID> requireDeliveryUserId() {
        return Mono.zip(SecurityContextUtils.getCurrentUserId(), SecurityContextUtils.getCurrentUserRole())
                .flatMap(tuple -> {
                    if (!"DELIVERY".equalsIgnoreCase(tuple.getT2())) {
                        return Mono.error(new UnauthorizedException("Delivery role required"));
                    }
                    return Mono.just(tuple.getT1());
                });
    }

    private Mono<Optional<FilePart>> readProofPhoto(ServerRequest req) {
        return req.multipartData()
                .map(parts -> {
                    var part = parts.getFirst("proofPhoto");
                    if (part == null) {
                        part = parts.getFirst("file");
                    }
                    if (part instanceof FilePart filePart) {
                        return Optional.of(filePart);
                    }
                    return Optional.<FilePart>empty();
                })
                .onErrorReturn(Optional.empty());
    }
}