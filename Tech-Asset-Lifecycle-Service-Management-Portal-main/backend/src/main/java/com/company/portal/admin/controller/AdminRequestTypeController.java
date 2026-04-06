package com.company.portal.admin.controller;

import com.company.portal.admin.dto.RequestTypeRequest;
import com.company.portal.admin.dto.RequestTypeResponse;
import com.company.portal.admin.service.AdminRequestTypeService;
import com.company.portal.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/request-types")
@Tag(name = "Admin - Request Types", description = "Request type management (ADMIN only)")
public class AdminRequestTypeController {

    private final AdminRequestTypeService service;

    public AdminRequestTypeController(AdminRequestTypeService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all request types")
    public ResponseEntity<ApiResponse<List<RequestTypeResponse>>> listAll() {
        return ResponseEntity.ok(ApiResponse.of(service.listAll(), "Request types retrieved"));
    }

    @PostMapping
    @Operation(summary = "Create a new request type")
    public ResponseEntity<ApiResponse<RequestTypeResponse>> create(
            @Valid @RequestBody RequestTypeRequest request,
            @AuthenticationPrincipal String callerEmail
    ) {
        return ResponseEntity.ok(ApiResponse.of(service.create(request, callerEmail), "Request type created"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a request type")
    public ResponseEntity<ApiResponse<RequestTypeResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody RequestTypeRequest request,
            @AuthenticationPrincipal String callerEmail
    ) {
        return ResponseEntity.ok(ApiResponse.of(service.update(id, request, callerEmail), "Request type updated"));
    }

    @PatchMapping("/{id}/toggle-active")
    @Operation(summary = "Toggle active status of a request type")
    public ResponseEntity<ApiResponse<RequestTypeResponse>> toggleActive(
            @PathVariable Long id,
            @AuthenticationPrincipal String callerEmail
    ) {
        return ResponseEntity.ok(ApiResponse.of(service.toggleActive(id, callerEmail), "Request type toggled"));
    }
}
