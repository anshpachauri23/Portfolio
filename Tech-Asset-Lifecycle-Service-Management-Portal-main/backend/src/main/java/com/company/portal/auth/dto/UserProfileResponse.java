package com.company.portal.auth.dto;

public record UserProfileResponse(
        Long id,
        String name,
        String email,
        String employeeCode,
        String role,
        Long departmentId,
        String departmentName
) {}
