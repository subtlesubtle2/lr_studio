package studio.lingrui.platform.common.core.response.enums;

import lombok.Getter;

/**
 * @author 2
 * @date 2026/3/12
 * @description: 返回码枚举类
 */
@Getter
public enum ResponseCode {
    /*调用成功返回码*/
    COMMON_SUCCESS(0,"完成",true),
    /*通用错误返回码*/
    COMMON_FAIL(1,"错误"),
    COMMON_TEST(2,"测试接口,未完成"),
    COMMON_ILLEGAL_PARAMS(3,"参数校验不通过"),
    ;
    private final Integer code;
    private final String message;
    private final Boolean status;

    ResponseCode(int code, String message){
        this.code=code;
        this.message=message;
        this.status=false;
    }

    ResponseCode(int code, String message, boolean status) {
        this.code=code;
        this.message=message;
        this.status=status;
    }
}
