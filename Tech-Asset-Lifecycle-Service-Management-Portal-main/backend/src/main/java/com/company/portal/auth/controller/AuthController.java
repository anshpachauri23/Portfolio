package com.company.portal.auth.controller;

import com.company.portal.auth.dto.LoginRequest;
import com.company.portal.auth.dto.LoginResponse;
import com.company.portal.auth.dto.UserProfileResponse;
import com.company.portal.auth.service.AuthService;
import com.company.portal.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login, profile, and logout endpoints")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and receive a JWT token")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.of(response, "Login successful"));
    }

    @GetMapping("/me")
    @Operation(summary = "Get the current authenticated user's profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> me(Authentication authentication) {
        UserProfileResponse profile = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(ApiResponse.of(profile, "Profile retrieved"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout (stateless — client should discard the token)")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.of(null, "Logged out successfully"));
    }
}
