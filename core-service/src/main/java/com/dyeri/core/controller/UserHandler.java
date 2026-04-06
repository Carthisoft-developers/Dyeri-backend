// com/dyeri/core/interfaces/rest/handlers/UserHandler.java
package com.dyeri.core.interfaces.rest.handlers;

import com.dyeri.core.application.bean.request.RegisterRequest;
import com.dyeri.core.application.bean.request.UpdateProfileRequest;
import com.dyeri.core.domain.services.UserService;
import com.dyeri.core.infrastructure.security.SecurityContextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserHandler {
    private final UserService userService;

    public Mono<ServerResponse> register(ServerRequest req) {
        return req.bodyToMono(RegisterRequest.class)
                .flatMap(userService::register)
                .flatMap(r -> ServerResponse.status(HttpStatus.CREATED).bodyValue(r));
    }

    public Mono<ServerResponse> getMe(ServerRequest req) {
        return SecurityContextUtils.getCurrentUserId()
                .flatMap(userService::getProfile)
                .flatMap(r -> ServerResponse.ok().bodyValue(r));
    }

    public Mono<ServerResponse> updateMe(ServerRequest req) {
        return SecurityContextUtils.getCurrentUserId()
                .flatMap(uid -> req.bodyToMono(UpdateProfileRequest.class)
                        .flatMap(body -> userService.updateProfile(uid, body)))
                .flatMap(r -> ServerResponse.ok().bodyValue(r));
    }

    public Mono<ServerResponse> uploadAvatar(ServerRequest req) {
        return SecurityContextUtils.getCurrentUserId()
                .flatMap(uid -> req.multipartData()
                        .map(parts -> (FilePart) parts.getFirst("file"))
                        .flatMap(file -> userService.uploadAvatar(uid, file)))
                .flatMap(url -> ServerResponse.ok().bodyValue(Map.of("avatarUrl", url)));
    }
}
