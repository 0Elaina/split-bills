package com.split.common.exception;

import com.split.common.web.ApiErrorCode;

import lombok.Getter;

/**
 * 请求参数错误异常
 */
@Getter
public class BadRequestException extends RuntimeException {
    private final ApiErrorCode errorCode;

    /**
     * 构造函数
     * 
     * @param errorCode 业务错误码
     * @param message 用户提示信息
     */
    public BadRequestException(ApiErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
