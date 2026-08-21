package com.split.common.web;

/**
 * API 全局错误码枚举
 * 用于统一定义业务失败时的状态标识
 */
public enum ApiErrorCode {
    SUCCESS,
    VALIDATION_ERROR,
    INVALID_MEMBER_REFERENCE,
    LEDGER_NOT_FOUND,
    MEMBER_NOT_FOUND,
    EXPENSE_NOT_FOUND,
    MEMBER_NAME_CONFLICT,
    MEMBER_IN_USE,
    INTERNAL_ERROR
}