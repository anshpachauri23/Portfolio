package com.company.portal.audit.dto;

import java.time.Instant;

public record AuditLogResponse(
        Long id,
        String entityType,
        String entityId,
        String action,
        Long actorId,
        String actorName,
        String beforeJson,
        String afterJson,
        Instant createdAt
) {}
