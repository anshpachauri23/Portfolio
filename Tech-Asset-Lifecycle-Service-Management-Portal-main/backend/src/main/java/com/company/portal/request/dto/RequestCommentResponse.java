package com.company.portal.request.dto;

import java.time.Instant;

public record RequestCommentResponse(
        Long id,
        Long authorId,
        String authorName,
        String body,
        Instant createdAt
) {}
