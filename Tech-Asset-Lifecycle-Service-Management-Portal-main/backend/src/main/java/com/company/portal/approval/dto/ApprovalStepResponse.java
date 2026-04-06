package com.company.portal.approval.dto;

import java.time.Instant;

public record ApprovalStepResponse(
        Long id,
        Long requestId,
        String requestNumber,
        String requestTitle,
        Long approverId,
        String approverName,
        Long requesterId,
        String requesterName,
        String decision,
        String comment,
        Instant decidedAt,
        int sequenceOrder,
        Instant createdAt
) {}
