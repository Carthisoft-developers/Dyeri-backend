// com/cuisinvoisin/interfaces/rest/NotificationController.java
package com.cuisinvoisin.interfaces.rest;

import com.cuisinvoisin.application.bean.response.NotificationResponse;
import com.cuisinvoisin.application.bean.response.PageResponse;
import com.cuisinvoisin.domain.services.NotificationService;
import com.cuisinvoisin.shared.util.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.NOTIFICATIONS_BASE)
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "In-app notification management")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get notifications for the authenticated user")
    public ResponseEntity<PageResponse<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(notificationService.getNotifications(
                UUID.fromString(principal.getUsername()), PageRequest.of(page, size)));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<Void> markRead(@AuthenticationPrincipal UserDetails principal,
                                          @PathVariable UUID id) {
        notificationService.markAsRead(UUID.fromString(principal.getUsername()), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal UserDetails principal) {
        notificationService.markAllAsRead(UUID.fromString(principal.getUsername()));
        return ResponseEntity.noContent().build();
    }
}
