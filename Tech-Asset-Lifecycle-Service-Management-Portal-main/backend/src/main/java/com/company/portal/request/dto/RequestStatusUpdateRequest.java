package com.company.portal.request.dto;

import jakarta.validation.constraints.NotBlank;

public record RequestStatusUpdateRequest(
        @NotBlank(message = "Status is required")
        String status,

        String notes
) {}
