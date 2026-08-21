package com.split.common.exception;

import com.split.common.web.ApiErrorCode;

import lombok.Getter;

/**
 * 冲突异常
 */
@Getter
public class ConflictException extends RuntimeException {
    private final ApiErrorCode errorCode;

    public ConflictException(ApiErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
