package com.company.portal.dashboard.controller;

import com.company.portal.common.dto.ApiResponse;
import com.company.portal.common.exception.ResourceNotFoundException;
import com.company.portal.dashboard.dto.*;
import com.company.portal.dashboard.service.DashboardService;
import com.company.portal.user.domain.User;
import com.company.portal.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Reporting and metrics")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    public DashboardController(DashboardService dashboardService, UserRepository userRepository) {
        this.dashboardService = dashboardService;
        this.userRepository = userRepository;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'MANAGER')")
    @Operation(summary = "Dashboard summary: open request count by status, asset count by status")
    public ResponseEntity<ApiResponse<DashboardSummary>> getSummary(Authentication authentication) {
        Long managerUserId = resolveManagerScope(authentication.getName());
        return ResponseEntity.ok(ApiResponse.of(
                dashboardService.getSummary(managerUserId), "Summary retrieved"));
    }

    @GetMapping("/request-trends")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'MANAGER')")
    @Operation(summary = "Request counts grouped by month for the last 6 months")
    public ResponseEntity<ApiResponse<List<RequestTrend>>> getRequestTrends(Authentication authentication) {
        Long managerUserId = resolveManagerScope(authentication.getName());
        return ResponseEntity.ok(ApiResponse.of(
                dashboardService.getRequestTrends(managerUserId), "Trends retrieved"));
    }

    @GetMapping("/assets-by-status")
    @Operation(summary = "Asset counts per status")
    public ResponseEntity<ApiResponse<List<StatusCount>>> getAssetsByStatus() {
        return ResponseEntity.ok(ApiResponse.of(
                dashboardService.getAssetsByStatus(), "Assets by status retrieved"));
    }

    @GetMapping("/sla-aging")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'MANAGER')")
    @Operation(summary = "Requests grouped by days past due_date")
    public ResponseEntity<ApiResponse<List<SlaAgingBucket>>> getSlaAging(Authentication authentication) {
        Long managerUserId = resolveManagerScope(authentication.getName());
        return ResponseEntity.ok(ApiResponse.of(
                dashboardService.getSlaAging(managerUserId), "SLA aging retrieved"));
    }

    private Long resolveManagerScope(String callerEmail) {
        User caller = userRepository.findByEmail(callerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + callerEmail));
        if (caller.getRole().name().equals("MANAGER")) {
            return caller.getId();
        }
        return null;
    }
}
