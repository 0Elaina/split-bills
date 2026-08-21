package com.split.common.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.split.common.exception.BadRequestException;
import com.split.common.exception.NotFoundException;
import com.split.common.exception.ConflictException;

import lombok.extern.slf4j.Slf4j;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理请求参数错误异常
     *
     * @param e 异常对象
     * @return 错误响应结果
     */
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBadRequestException(BadRequestException e) {
        log.warn("请求参数错误: {}", e.getMessage());
        return Result.error(e.getErrorCode(), e.getMessage());
    }

    /**
     * 资源未找到异常处理
     * 
     * @param e 异常对象
     * @return 错误响应结果
     */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNotFound(NotFoundException e) {
        log.warn("资源未找到: {}", e.getMessage());
        return Result.error(e.getErrorCode(), e.getMessage());
    }

    /**
     * 资源冲突异常处理
     * 
     * @param e 异常对象
     * @return 错误响应结果
     */
    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Result<Void> handleConfict(ConflictException e) {
        log.warn("资源冲突: {}", e.getMessage());
        return Result.error(e.getErrorCode(), e.getMessage());
    }

    /**
     * 处理 Bean Validation 参数校验失败异常
     * 比如 @NotBlank, @NotNull 校验不通过时抛出的异常
     * 
     * @param e 异常对象
     * @return 错误响应结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        // 获取第一条校验失败的错误提示
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("参数校验失败: {}", message);
        return Result.error(ApiErrorCode.VALIDATION_ERROR, message);
    }

    /**
     * 未捕获的全局系统异常兜底
     * 防止程序报错堆栈暴露给前端
     * 
     * @param e 异常对象
     * @return 错误响应结果
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        // 发生未预期错误时，必须用 log.error 打印完整堆栈，方便排查 BUG
        log.error("系统内部异常", e);
        return Result.error(ApiErrorCode.INTERNAL_ERROR, "系统繁忙, 请稍后重试");
    }
}
