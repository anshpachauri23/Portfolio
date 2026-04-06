package com.company.portal.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        String employeeCode,
        String role,
        Long departmentId,
        Long managerId
) {}
