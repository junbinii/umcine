package com.umc.movie.dto;

public record ApiResponse<T>(boolean isSuccess, String code, String message, T result) {
    public static <T> ApiResponse<T> created(String message, T result) {
        return new ApiResponse<>(true, "COMMON201", message, result);
    }

    public static ApiResponse<Void> ok(String message) {
        return new ApiResponse<>(true, "COMMON200", message, null);
    }

    public static ApiResponse<Void> failure(int status, String message) {
        return new ApiResponse<>(false, "COMMON" + status, message, null);
    }
}
