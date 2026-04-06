package com.company.portal.admin.dto;

import java.time.Instant;

public record RequestTypeResponse(
        Long id,
        String name,
        String description,
        boolean approvalRequired,
        boolean active,
        Instant createdAt
) {}
