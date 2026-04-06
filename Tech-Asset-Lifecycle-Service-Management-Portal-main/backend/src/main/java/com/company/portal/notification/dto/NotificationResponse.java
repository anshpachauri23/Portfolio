package com.company.portal.notification.dto;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        String title,
        String body,
        boolean read,
        String entityType,
        String entityId,
        Instant createdAt
) {}
