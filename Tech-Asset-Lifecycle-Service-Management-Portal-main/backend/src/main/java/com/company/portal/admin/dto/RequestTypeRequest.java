package com.company.portal.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record RequestTypeRequest(
        @NotBlank String name,
        String description,
        boolean approvalRequired
) {}
