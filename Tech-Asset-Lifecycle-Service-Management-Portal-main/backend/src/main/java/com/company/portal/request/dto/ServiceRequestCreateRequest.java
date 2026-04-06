package com.company.portal.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ServiceRequestCreateRequest(
        @NotNull(message = "Request type is required")
        Long requestTypeId,

        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        @NotBlank(message = "Description is required")
        @Size(min = 10, message = "Description must be at least 10 characters")
        String description,

        Long assetId,

        @NotBlank(message = "Priority is required")
        String priority,

        LocalDate dueDate
) {}
