package com.pet.community.web;

import com.pet.common.entity.Result;
import com.pet.community.exception.AdminAuthorizationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 社区 Web 层统一异常：未登录、参数错误、校验失败。 */
@RestControllerAdvice(basePackages = "com.pet.community.web")
public class CommunityGlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleUnauthorized(IllegalStateException e) {
        if ("UNAUTHORIZED".equals(e.getMessage())) {
            return new Result<>(401, "未登录或令牌无效", null);
        }
        return new Result<>(401, e.getMessage(), null);
    }

    @ExceptionHandler(AdminAuthorizationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAdminDenied(AdminAuthorizationException e) {
        return new Result<>(403, e.getMessage(), null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBadRequest(IllegalArgumentException e) {
        return new Result<>(400, e.getMessage(), null);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidation(Exception e) {
        return new Result<>(400, "参数校验失败", null);
    }
}
