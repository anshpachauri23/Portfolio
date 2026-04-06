package com.company.portal.request.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record ServiceRequestResponse(
        Long id,
        String requestNumber,
        Long requestTypeId,
        String requestTypeName,
        String title,
        String description,
        Long requesterId,
        String requesterName,
        Long assetId,
        String priority,
        String status,
        Long assignedToId,
        String assignedToName,
        boolean approvalRequired,
        LocalDate dueDate,
        Instant closedAt,
        Instant createdAt,
        Instant updatedAt,
        List<RequestCommentResponse> comments
) {}
