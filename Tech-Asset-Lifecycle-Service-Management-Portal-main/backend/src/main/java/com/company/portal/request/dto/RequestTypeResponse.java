package com.company.portal.request.dto;

public record RequestTypeResponse(
        Long id,
        String name,
        String description,
        boolean approvalRequired,
        boolean active
) {}
