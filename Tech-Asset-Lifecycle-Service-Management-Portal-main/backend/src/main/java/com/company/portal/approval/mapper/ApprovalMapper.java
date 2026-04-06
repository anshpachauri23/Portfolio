package com.company.portal.approval.mapper;

import com.company.portal.approval.domain.ApprovalStep;
import com.company.portal.approval.dto.ApprovalStepResponse;

public final class ApprovalMapper {

    private ApprovalMapper() {}

    public static ApprovalStepResponse toResponse(ApprovalStep step) {
        return new ApprovalStepResponse(
                step.getId(),
                step.getRequest().getId(),
                step.getRequest().getRequestNumber(),
                step.getRequest().getTitle(),
                step.getApprover().getId(),
                step.getApprover().getName(),
                step.getRequest().getRequester().getId(),
                step.getRequest().getRequester().getName(),
                step.getDecision().name(),
                step.getComment(),
                step.getDecidedAt(),
                step.getSequenceOrder(),
                step.getCreatedAt()
        );
    }
}
