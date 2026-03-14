package studio.lingrui.platform.common.core.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import studio.lingrui.platform.common.core.response.Response;
import studio.lingrui.platform.common.core.response.enums.ResponseCode;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author 2
 * @date 2026/3/12
 * @description: 全局异常处理器
 */

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 通用异常处理器
     * @param e e
     * @return {@link ResponseEntity}<{@link Response}<{@link Void}>>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Void>> ExceptionHandler(Exception e){
        log.error("ERROR :{}",getStackTraceAsString(e));
        Response<Void> res = new Response.Builder<Void>()
                .code(ResponseCode.COMMON_FAIL)
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    }

    /**
     * 将异常的完整堆栈信息转换为字符串。
     * @param throwable 异常对象
     * @return 完整的堆栈字符串
     */
    private String getStackTraceAsString(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}