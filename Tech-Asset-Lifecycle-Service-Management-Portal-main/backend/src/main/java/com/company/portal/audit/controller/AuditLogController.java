package com.company.portal.audit.controller;

import com.company.portal.audit.dto.AuditLogResponse;
import com.company.portal.audit.service.AuditService;
import com.company.portal.common.dto.ApiResponse;
import com.company.portal.common.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/audit-logs")
@Tag(name = "Audit Logs", description = "Immutable audit log (ADMIN only)")
public class AuditLogController {

    private final AuditService auditService;

    public AuditLogController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @Operation(summary = "Query audit logs (ADMIN only)")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getAuditLogs(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long actorId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                auditService.getAuditLogs(entityType, actorId, pageable),
                "Audit logs retrieved"
        ));
    }
}
