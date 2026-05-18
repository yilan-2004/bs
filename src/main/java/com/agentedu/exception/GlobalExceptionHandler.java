package com.agentedu.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.agentedu.common.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException exception) {
        log.warn("Business exception code={}, message={}", exception.getCode(), exception.getMessage());
        return Result.fail(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(NotLoginException.class)
    public Result<Void> handleNotLoginException(NotLoginException exception) {
        log.warn("Not login: {}", exception.getMessage());
        return Result.fail(401, "未登录或登录状态已失效，请重新登录");
    }

    @ExceptionHandler(NotPermissionException.class)
    public Result<Void> handleNotPermissionException(NotPermissionException exception) {
        log.warn("No permission: {}", exception.getMessage());
        return Result.fail(403, "无权限访问该资源");
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public Result<Void> handleValidationException(Exception exception) {
        log.warn("Validation exception: {}", exception.getMessage());
        return Result.fail("参数校验失败，请检查输入内容");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception exception) {
        log.error("Unhandled exception type={}, message={}", exception.getClass().getSimpleName(), exception.getMessage());
        return Result.fail("系统异常，请稍后重试");
    }
}
