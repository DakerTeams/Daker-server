package com.daker.global.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

    private boolean success;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(true, data);
    }

    public static ApiResponse<ErrorDetail> fail(String message) {
        return new ApiResponse<>(false, new ErrorDetail(message));
    }

    @Getter
    @AllArgsConstructor
    public static class ErrorDetail {
        private String message;
    }
}
