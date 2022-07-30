package com.yhl.gj.commons.enums;

public enum ErrorEnum {

    SYS_ERROR(500, "系统异常"),

    PARAM_ERROR(1000,"参数错误" ),
            ;


    private final Integer errorCode;
    private final String errorMsg;

    ErrorEnum(Integer errorCode, String errorMsg){
        this.errorCode=errorCode;
        this.errorMsg=errorMsg;
    }


    public Integer getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public static ErrorEnum findByCode(Integer code){
        for (ErrorEnum value : ErrorEnum.values()) {
            if(value.getErrorCode().equals(code)){
                return value;
            }
        }
        return null;
    }
}
