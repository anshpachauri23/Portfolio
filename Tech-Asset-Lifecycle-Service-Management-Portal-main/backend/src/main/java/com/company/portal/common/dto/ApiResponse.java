package com.company.portal.common.dto;

import java.time.Instant;

public record ApiResponse<T>(T data, String message, String timestamp) {

    public static <T> ApiResponse<T> of(T data, String message) {
        return new ApiResponse<>(data, message, Instant.now().toString());
    }
}
