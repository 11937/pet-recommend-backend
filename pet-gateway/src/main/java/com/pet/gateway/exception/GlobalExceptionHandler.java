package com.pet.gateway.exception;



import com.pet.gateway.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 网关必须返回 Mono<Result>
    @ExceptionHandler(RuntimeException.class)
    public Mono<Result<Object>> handleRuntimeException(RuntimeException e) {
        log.error("网关异常：{}", e.getMessage());
        // 外面包一层 Mono.just( )
        return Mono.just(Result.fail(500, e.getMessage()));
    }
}