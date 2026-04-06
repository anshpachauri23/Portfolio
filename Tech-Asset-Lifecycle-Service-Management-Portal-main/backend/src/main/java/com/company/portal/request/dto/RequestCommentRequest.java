package com.company.portal.request.dto;

import jakarta.validation.constraints.NotBlank;

public record RequestCommentRequest(
        @NotBlank(message = "Comment body is required")
        String body
) {}
