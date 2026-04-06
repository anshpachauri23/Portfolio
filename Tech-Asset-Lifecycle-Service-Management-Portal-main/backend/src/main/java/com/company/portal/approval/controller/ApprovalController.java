package com.company.portal.approval.controller;

import com.company.portal.approval.dto.ApprovalActionRequest;
import com.company.portal.approval.dto.ApprovalStepResponse;
import com.company.portal.approval.service.ApprovalService;
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

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Approvals", description = "Approval workflow endpoints")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @PostMapping("/requests/{id}/approve")
    @Operation(summary = "Approve a pending request (MANAGER only)")
    public ResponseEntity<ApiResponse<ApprovalStepResponse>> approve(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalActionRequest request,
            @AuthenticationPrincipal String approverEmail
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                approvalService.approve(id, request, approverEmail),
                "Request approved"
        ));
    }

    @PostMapping("/requests/{id}/reject")
    @Operation(summary = "Reject a pending request (MANAGER only)")
    public ResponseEntity<ApiResponse<ApprovalStepResponse>> reject(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalActionRequest request,
            @AuthenticationPrincipal String approverEmail
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                approvalService.reject(id, request, approverEmail),
                "Request rejected"
        ));
    }

    @GetMapping("/approvals/pending")
    @Operation(summary = "Get pending approvals for the current manager")
    public ResponseEntity<ApiResponse<PagedResponse<ApprovalStepResponse>>> getPendingApprovals(
            @AuthenticationPrincipal String approverEmail,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                approvalService.getPendingApprovals(approverEmail, pageable),
                "Pending approvals retrieved"
        ));
    }

    @GetMapping("/requests/{id}/approvals")
    @Operation(summary = "Get approval history for a request")
    public ResponseEntity<ApiResponse<List<ApprovalStepResponse>>> getApprovalHistory(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                approvalService.getApprovalHistory(id),
                "Approval history retrieved"
        ));
    }
}
