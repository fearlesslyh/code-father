package com.lyh.codefather.exception;

import com.lyh.codefather.common.BaseResponse;
import com.lyh.codefather.common.ResultUtils;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @version 1.0
 * @since 2025/11/10 22:56
 */

@Slf4j
@RestControllerAdvice
@Hidden
/**
 * 全局异常处理器
 */
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage());
    }
    
    /**
     * 处理SSE流式请求中的异常
     * 
     * @param e 异常
     * @return SSE响应
     */
    @ExceptionHandler({dev.langchain4j.exception.TimeoutException.class, java.net.SocketTimeoutException.class})
    public SseEmitter handleSseTimeoutException(Exception e) {
        log.error("SSE超时异常", e);
        
        SseEmitter emitter = new SseEmitter();
        try {
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data("{\"status\":\"error\",\"message\":\"请求超时，请稍后重试\"}")
                    .id(String.valueOf(System.currentTimeMillis())));
            emitter.complete();
        } catch (IOException ioException) {
            log.error("发送SSE错误消息失败", ioException);
            emitter.completeWithError(ioException);
        }
        
        return emitter;
    }
}
