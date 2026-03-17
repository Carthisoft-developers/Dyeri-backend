// com/cuisinvoisin/interfaces/rest/UserController.java
package com.cuisinvoisin.interfaces.rest;

import com.cuisinvoisin.application.bean.request.UpdateProfileRequest;
import com.cuisinvoisin.application.bean.response.UserResponse;
import com.cuisinvoisin.domain.services.UserService;
import com.cuisinvoisin.shared.util.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.USERS_BASE)
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile management")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get authenticated user's profile")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(userService.getProfile(UUID.fromString(principal.getUsername())));
    }

    @PatchMapping("/me")
    @Operation(summary = "Update authenticated user's profile")
    public ResponseEntity<UserResponse> updateMe(@AuthenticationPrincipal UserDetails principal,
                                                  @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(
                UUID.fromString(principal.getUsername()), request));
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a new avatar image")
    public ResponseEntity<Map<String, String>> uploadAvatar(
            @AuthenticationPrincipal UserDetails principal,
            @RequestPart("file") MultipartFile file) {
        String url = userService.uploadAvatar(UUID.fromString(principal.getUsername()), file);
        return ResponseEntity.ok(Map.of("avatarUrl", url));
    }
}
