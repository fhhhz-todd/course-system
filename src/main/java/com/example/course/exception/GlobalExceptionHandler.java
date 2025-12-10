package com.example.course.exception;

import com.example.course.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<String> exceptionHandler(Exception ex) {
        log.error("系统异常：", ex);
        return Result.error("系统异常，请联系管理员");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<String> illegalArgumentExceptionHandler(IllegalArgumentException ex) {
        log.error("参数异常：", ex);
        return Result.error(ex.getMessage());
    }
}