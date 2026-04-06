package com.company.portal.asset.controller;

import com.company.portal.asset.domain.AssetStatus;
import com.company.portal.asset.dto.*;
import com.company.portal.asset.service.AssetService;
import com.company.portal.common.dto.ApiResponse;
import com.company.portal.common.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@Tag(name = "Assets", description = "Asset lifecycle management")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping
    @Operation(summary = "List assets with optional filters")
    public ResponseEntity<ApiResponse<PagedResponse<AssetResponse>>> getAssets(
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(required = false) String assetType,
            @RequestParam(required = false) Long assignedUserId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal String callerEmail
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                assetService.getAssets(status, assetType, assignedUserId, pageable, callerEmail),
                "Assets retrieved"
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single asset by ID")
    public ResponseEntity<ApiResponse<AssetResponse>> getAsset(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(assetService.getAsset(id), "Asset retrieved"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    @Operation(summary = "Create a new asset (ADMIN/TECHNICIAN only)")
    public ResponseEntity<ApiResponse<AssetResponse>> createAsset(@Valid @RequestBody AssetRequest request) {
        AssetResponse created = assetService.createAsset(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(created, "Asset created"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an asset (ADMIN/TECHNICIAN only)")
    public ResponseEntity<ApiResponse<AssetResponse>> updateAsset(
            @PathVariable Long id,
            @Valid @RequestBody AssetRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.of(assetService.updateAsset(id, request), "Asset updated"));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update asset status with transition validation")
    public ResponseEntity<ApiResponse<AssetResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody AssetStatusUpdateRequest request,
            @AuthenticationPrincipal String actorEmail
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                assetService.updateStatus(id, request, actorEmail),
                "Asset status updated"
        ));
    }

    @PostMapping("/{id}/assign")
    @Operation(summary = "Assign asset to a user")
    public ResponseEntity<ApiResponse<AssetResponse>> assignAsset(
            @PathVariable Long id,
            @Valid @RequestBody AssetAssignRequest request,
            @AuthenticationPrincipal String actorEmail
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                assetService.assignAsset(id, request, actorEmail),
                "Asset assigned"
        ));
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Get asset history timeline")
    public ResponseEntity<ApiResponse<List<AssetHistoryResponse>>> getHistory(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(assetService.getHistory(id), "Asset history retrieved"));
    }
}
