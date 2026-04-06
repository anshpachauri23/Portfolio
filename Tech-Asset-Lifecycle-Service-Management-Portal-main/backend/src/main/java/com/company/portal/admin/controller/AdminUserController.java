package com.company.portal.admin.controller;

import com.company.portal.admin.dto.UserCreateRequest;
import com.company.portal.admin.dto.UserResponse;
import com.company.portal.admin.dto.UserUpdateRequest;
import com.company.portal.admin.service.AdminUserService;
import com.company.portal.common.dto.ApiResponse;
import com.company.portal.common.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Admin - Users", description = "User management (ADMIN only)")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    @Operation(summary = "List all users")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> listUsers(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.of(adminUserService.listUsers(pageable), "Users retrieved"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(adminUserService.getUser(id), "User retrieved"));
    }

    @PostMapping
    @Operation(summary = "Create a new user")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserCreateRequest request,
            @AuthenticationPrincipal String callerEmail
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                adminUserService.createUser(request, callerEmail), "User created"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a user")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal String callerEmail
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                adminUserService.updateUser(id, request, callerEmail), "User updated"));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a user")
    public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(
            @PathVariable Long id,
            @AuthenticationPrincipal String callerEmail
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                adminUserService.deactivateUser(id, callerEmail), "User deactivated"));
    }
}
