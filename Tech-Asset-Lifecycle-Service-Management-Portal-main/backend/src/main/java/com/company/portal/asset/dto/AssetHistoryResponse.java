package com.company.portal.asset.dto;

import java.time.Instant;

public record AssetHistoryResponse(
        Long id,
        String eventType,
        String fromStatus,
        String toStatus,
        Long actorId,
        String actorName,
        String notes,
        Instant createdAt
) {}
