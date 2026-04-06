package com.company.portal.asset.dto;

import jakarta.validation.constraints.NotBlank;

public record AssetStatusUpdateRequest(
        @NotBlank(message = "New status is required")
        String status,

        String notes
) {}
