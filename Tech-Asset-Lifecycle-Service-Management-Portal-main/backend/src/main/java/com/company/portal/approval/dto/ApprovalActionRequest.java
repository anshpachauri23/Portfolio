package com.company.portal.approval.dto;

import jakarta.validation.constraints.Size;

public record ApprovalActionRequest(
        @Size(max = 2000, message = "Comment must be 2000 characters or fewer")
        String comment
) {}
