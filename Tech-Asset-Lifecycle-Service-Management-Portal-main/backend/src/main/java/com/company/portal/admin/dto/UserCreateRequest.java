package com.company.portal.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserCreateRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        String employeeCode,
        @NotBlank String password,
        @NotNull String role,
        Long departmentId,
        Long managerId
) {}
