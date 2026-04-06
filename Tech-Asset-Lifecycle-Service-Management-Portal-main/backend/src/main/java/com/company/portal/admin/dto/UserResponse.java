package com.company.portal.admin.dto;

import java.time.Instant;

public record UserResponse(
        Long id,
        String name,
        String email,
        String employeeCode,
        String role,
        Long departmentId,
        String departmentName,
        Long managerId,
        String managerName,
        boolean active,
        Instant createdAt
) {}
