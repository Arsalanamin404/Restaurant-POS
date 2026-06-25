package com.arsalan.tenanttable.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ApiResponse<T> {

    private int statusCode;
    private boolean success;
    private String message;
    private T data;
    private T errors;
    private LocalDateTime timestamp;
    private String path;

    public ApiResponse(
            int statusCode,
            boolean success,
            String message,
            T data,
            T errors,
            String path
    ) {
        this.statusCode = statusCode;
        this.success = success;
        this.message = message;
        this.data = data;
        this.errors = errors;
        this.timestamp = LocalDateTime.now();
        this.path = path;
    }

    public static <T> ApiResponse<T> success(int statusCode, String message, T data, String path) {
        return new ApiResponse<>(statusCode, true, message, data, null, path);
    }

    public static <T> ApiResponse<T> failure(int statusCode, String message, T errors, String path) {
        return new ApiResponse<>(statusCode, false, message, null, errors, path);
    }
}