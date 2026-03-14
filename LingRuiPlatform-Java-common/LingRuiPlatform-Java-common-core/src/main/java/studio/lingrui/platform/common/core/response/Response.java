package studio.lingrui.platform.common.core.response;

import lombok.Getter;
import lombok.Setter;
import studio.lingrui.platform.common.core.response.enums.ResponseCode;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 * @author 2
 * @date 2026/3/12
 * @description: 通用返回类
 */
@Setter
@Getter
public class Response<T> {
    private Integer code;
    private T data;
    private String message;
    private boolean status;
    private Long len;
    private Long ts; // timestamp 时间戳
    private String dt; // datetime 当前时间

    private Response() {}

    Response(Builder<T> builder) {
        this.data = builder.data;
        this.len = builder.len;
        this.code = builder.getResponseCode().getCode();
        this.status = builder.getResponseCode().getStatus();

        //传入message则使用,默认使用枚举值
        if (builder.message != null){
            this.message = builder.message;
        }else {
            this.message = builder.getResponseCode().getMessage();
        }

        //无关builder,向前端传入时间戳和当前时间
        LocalDateTime nowTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.dt = nowTime.format(formatter);
        this.ts = Instant.now().toEpochMilli();
    }


    @Getter
    public static class Builder<T>{
        private ResponseCode responseCode;
        private T data;
        private Long len;
        private String message;

        public Builder<T> code(ResponseCode code) {
            this.responseCode = code;
            return this;
        }

        public Builder<T> data(T data) {
            this.data = data;
            return this;
        }

        public Builder<T> len(Long len) {
            this.len = len;
            return this;
        }

        public Builder<T> message(String message) {
            this.message = message;
            return this;
        }

        public Response<T> build(){
            return new Response<>(this);
        }
    }

}
