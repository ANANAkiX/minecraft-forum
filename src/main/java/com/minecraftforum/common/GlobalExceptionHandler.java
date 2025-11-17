package com.minecraftforum.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("参数错误: {}", e.getMessage());
        return Result.error(400, e.getMessage());
    }
    
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e) {
        // 对于已知的业务异常，只记录警告
        String message = e.getMessage();
        if (message != null && (message.contains("已存在") || 
                                message.contains("不存在") || 
                                message.contains("错误") ||
                                message.contains("失败") ||
                                message.contains("已被禁用") ||
                                message.contains("不可用"))) {
            log.warn("业务异常: {}", message);
            return Result.error(400, message);
        }
        // 未知的运行时异常，记录错误
        log.error("运行时异常", e);
        return Result.error(500, "操作失败：" + message);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return Result.error(400, "参数校验失败：" + message);
    }
    
    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return Result.error(400, "参数绑定失败：" + message);
    }
    
    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public Result<?> handleAuthenticationException(AuthenticationCredentialsNotFoundException e) {
        return Result.error(401, "未登录或登录已过期");
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public Result<?> handleAccessDeniedException(AccessDeniedException e) {
        return Result.error(403, "没有权限访问该资源");
    }
    
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "系统异常：" + e.getMessage());
    }
}













