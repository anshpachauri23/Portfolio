package com.company.portal.asset.dto;

import jakarta.validation.constraints.NotNull;

public record AssetAssignRequest(
        @NotNull(message = "User ID is required")
        Long userId,

        String notes
) {}
