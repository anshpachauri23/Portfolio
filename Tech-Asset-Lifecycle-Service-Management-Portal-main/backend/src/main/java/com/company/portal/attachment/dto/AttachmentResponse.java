package com.company.portal.attachment.dto;

import java.time.Instant;

public record AttachmentResponse(
        Long id,
        String fileName,
        long fileSize,
        String contentType,
        Long uploadedById,
        String uploadedByName,
        Instant createdAt
) {}
