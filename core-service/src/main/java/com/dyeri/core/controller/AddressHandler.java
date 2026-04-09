// com/dyeri/core/interfaces/rest/handlers/AddressHandler.java
package com.dyeri.core.interfaces.rest.handlers;

import com.dyeri.core.application.bean.request.SaveAddressRequest;
import com.dyeri.core.domain.services.AddressService;
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
public class AddressHandler {
    private final AddressService addressService;

    public Mono<ServerResponse> getMyAddresses(ServerRequest req) {
        return SecurityContextUtils.getCurrentUserId()
                .flatMapMany(addressService::getAddresses)
                .collectList()
                .flatMap(addresses -> ServerResponse.ok().bodyValue(addresses));
    }

    public Mono<ServerResponse> saveAddress(ServerRequest req) {
        return SecurityContextUtils.getCurrentUserId()
                .flatMap(userId -> req.bodyToMono(SaveAddressRequest.class)
                        .flatMap(body -> addressService.saveAddress(userId, body)))
                .flatMap(address -> ServerResponse.status(HttpStatus.CREATED).bodyValue(address));
    }

    public Mono<ServerResponse> updateAddress(ServerRequest req) {
        UUID addressId = UUID.fromString(req.pathVariable("id"));
        return SecurityContextUtils.getCurrentUserId()
                .flatMap(userId -> req.bodyToMono(SaveAddressRequest.class)
                        .flatMap(body -> addressService.updateAddress(userId, addressId, body)))
                .flatMap(address -> ServerResponse.ok().bodyValue(address));
    }

    public Mono<ServerResponse> deleteAddress(ServerRequest req) {
        UUID addressId = UUID.fromString(req.pathVariable("id"));
        return SecurityContextUtils.getCurrentUserId()
                .flatMap(userId -> addressService.deleteAddress(userId, addressId))
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> setDefault(ServerRequest req) {
        UUID addressId = UUID.fromString(req.pathVariable("id"));
        return SecurityContextUtils.getCurrentUserId()
                .flatMap(userId -> addressService.setDefault(userId, addressId))
                .then(ServerResponse.noContent().build());
    }
}
