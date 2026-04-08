// com/dyeri/core/interfaces/rest/handlers/UserHandler.java
package com.dyeri.core.interfaces.rest.handlers;

import com.dyeri.core.application.bean.request.RegisterRequest;
import com.dyeri.core.application.bean.request.UpdateProfileRequest;
import com.dyeri.core.domain.services.UserService;
import com.dyeri.core.infrastructure.security.SecurityContextUtils;
import com.dyeri.core.shared.util.ApiConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

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
                        .flatMap(file -> userService.uploadAvatar(uid, file))
                        .thenReturn(uid))
                .flatMap(uid -> ServerResponse.ok().bodyValue(
                        Map.of("avatarUrl", ApiConstants.USERS_BASE + "/" + uid + "/avatar")));
    }

    public Mono<ServerResponse> getAvatarById(ServerRequest req) {
        UUID userId = UUID.fromString(req.pathVariable("id"));
        final String ifNoneMatch = req.headers().firstHeader(HttpHeaders.IF_NONE_MATCH);

        return userService.getAvatar(userId)
                .flatMap(bytes -> {
                    final String eTag = '"' + DigestUtils.md5DigestAsHex(bytes) + '"';
                    final CacheControl cacheControl = CacheControl.maxAge(Duration.ofDays(7)).cachePublic();

                    if (ifNoneMatch != null && ifNoneMatch.contains(eTag)) {
                        return ServerResponse.status(HttpStatus.NOT_MODIFIED)
                                .eTag(eTag)
                                .cacheControl(cacheControl)
                                .build();
                    }

                    return ServerResponse.ok()
                            .contentType(MediaType.IMAGE_JPEG)
                            .eTag(eTag)
                            .cacheControl(cacheControl)
                            .bodyValue(bytes);
                });
    }
}
