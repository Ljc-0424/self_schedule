package com.cjl.self_schedule.common.exception;

import com.cjl.self_schedule.common.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${debug:false}")
    private boolean debugMode;

    /**
     * 业务异常 — 返回对应错误码
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        log.warn("业务异常 - 路径: {} {}, 消息: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return Result.error(ex.getCode(), ex.getMessage());
    }

    /**
     * 参数校验异常（@Valid / @Validated）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败 - 路径: {} {}, 消息: {}", request.getMethod(), request.getRequestURI(), message);
        return Result.error(400, message);
    }

    /**
     * 缺少请求参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest request) {
        log.warn("缺少请求参数 - 路径: {} {}, 参数: {}", request.getMethod(), request.getRequestURI(), ex.getParameterName());
        return Result.error(400, "缺少必要参数: " + ex.getParameterName());
    }

    /**
     * HTTP 消息解析异常（JSON 格式错误等）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("HTTP消息解析异常 - 路径: {} {}, 原因: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        String message = debugMode ? "请求体解析失败: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()) : "请求体解析失败";
        return Result.error(400, message);
    }

    /**
     * 兜底异常处理
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleAllExceptions(Exception ex, HttpServletRequest request) {
        log.error("未捕获的异常 - 路径: {} {}, 类型: {}, 消息: {}", request.getMethod(), request.getRequestURI(), ex.getClass().getName(), ex.getMessage(), ex);
        String message = debugMode ? "系统内部错误: " + ex.getMessage() : "系统内部错误，请稍后重试";
        return Result.error(500, message);
    }
}
