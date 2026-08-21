package com.split.common.exception;

import com.split.common.web.ApiErrorCode;

import lombok.Getter;

/**
 * 资源未找到异常
 */
@Getter
public class NotFoundException extends RuntimeException {
    private final ApiErrorCode errorCode;

    public NotFoundException(ApiErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
