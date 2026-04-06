package com.company.portal.request.controller;

import com.company.portal.common.dto.ApiResponse;
import com.company.portal.common.dto.PagedResponse;
import com.company.portal.request.domain.RequestStatus;
import com.company.portal.request.dto.*;
import com.company.portal.request.service.ServiceRequestService;
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
@RequestMapping("/api/requests")
@Tag(name = "Service Requests", description = "Service request management")
public class ServiceRequestController {

    private final ServiceRequestService requestService;

    public ServiceRequestController(ServiceRequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    @Operation(summary = "Create a new service request")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> createRequest(
            @Valid @RequestBody ServiceRequestCreateRequest dto,
            @AuthenticationPrincipal String requesterEmail
    ) {
        ServiceRequestResponse created = requestService.createRequest(dto, requesterEmail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(created, "Request created"));
    }

    @GetMapping("/my")
    @Operation(summary = "Get the current user's own requests")
    public ResponseEntity<ApiResponse<PagedResponse<ServiceRequestResponse>>> getMyRequests(
            @AuthenticationPrincipal String email,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                requestService.getMyRequests(email, pageable),
                "Requests retrieved"
        ));
    }

    @GetMapping("/queue")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'MANAGER')")
    @Operation(summary = "Get the request queue (TECHNICIAN/MANAGER/ADMIN)")
    public ResponseEntity<ApiResponse<PagedResponse<ServiceRequestResponse>>> getQueue(
            @RequestParam(required = false) List<RequestStatus> status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                requestService.getQueue(status, pageable),
                "Request queue retrieved"
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single service request")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> getRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal String email
    ) {
        return ResponseEntity.ok(ApiResponse.of(requestService.getRequest(id, email), "Request retrieved"));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update request status (TECHNICIAN/ADMIN only)")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody RequestStatusUpdateRequest dto
    ) {
        return ResponseEntity.ok(ApiResponse.of(requestService.updateStatus(id, dto), "Status updated"));
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "Add a comment to a request")
    public ResponseEntity<ApiResponse<RequestCommentResponse>> addComment(
            @PathVariable Long id,
            @Valid @RequestBody RequestCommentRequest dto,
            @AuthenticationPrincipal String email
    ) {
        RequestCommentResponse comment = requestService.addComment(id, dto, email);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(comment, "Comment added"));
    }
}
