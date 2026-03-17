// com/cuisinvoisin/interfaces/rest/FollowController.java
package com.cuisinvoisin.interfaces.rest;

import com.cuisinvoisin.application.bean.response.CookSummaryResponse;
import com.cuisinvoisin.domain.services.FollowService;
import com.cuisinvoisin.shared.util.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.FOLLOWS_BASE)
@RequiredArgsConstructor
@Tag(name = "Follows", description = "Cook follow/unfollow management")
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{cookId}/toggle")
    @Operation(summary = "Follow or unfollow a cook")
    public ResponseEntity<Void> toggle(@AuthenticationPrincipal UserDetails principal,
                                        @PathVariable UUID cookId) {
        followService.toggleFollow(UUID.fromString(principal.getUsername()), cookId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "List followed cooks")
    public ResponseEntity<List<CookSummaryResponse>> getFollowedCooks(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(followService.getFollowedCooks(UUID.fromString(principal.getUsername())));
    }
}
