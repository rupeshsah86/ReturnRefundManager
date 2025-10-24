package com.ecommerce.returnmanager.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String timestamp;
    private String error;

    // Success static methods
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Operation successful", data, LocalDateTime.now().toString(), null);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now().toString(), null);
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, LocalDateTime.now().toString(), null);
    }

    // Error static methods
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, null, LocalDateTime.now().toString(), message);
    }

    public static <T> ApiResponse<T> error(String message, String error) {
        return new ApiResponse<>(false, null, null, LocalDateTime.now().toString(), error);
    }
}