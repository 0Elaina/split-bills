package com.split.common.web;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Result<T> {
    // 稳定结果码，成功时固定为 SUCCESS
    private String code;

    // 可直接展示给用户的中文提示
    private String message;

    // 成功时承载的业务数据，失败时通常为 null
    private T data;

    /**
     * 响应成功, 无数据返回
     */
    public static <T> Result<T> success() {
        return Result.success(null);
    }

    /**
     * 响应成功, 有数据返回
     * 
     * @param <T> 数据类型
     * @param data 业务数据
     * @return 响应结果
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>(
            ApiErrorCode.SUCCESS.name(),
            "操作成功",
            data
        );
        return result;
    }

    /**
     * 响应失败, 有错误码和错误信息返回
     * 
     * @param <T> 数据类型
     * @param code 错误码
     * @param message 错误信息
     * @return 响应结果
     */
    public static <T> Result<T> error(ApiErrorCode code, String message) {
        Result<T> result = new Result<>(
            code.name(),
            message,
            null
        );
        return result;
    }
}
