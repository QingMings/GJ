package com.yhl.gj.commons.enums;

public enum ErrorEnum {

    SYS_ERROR("500", "系统异常"),

    PARAM_ERROR("1000","参数错误" ),
            ;


    private final String errorCode;
    private final String errorMsg;

    ErrorEnum(String errorCode, String errorMsg){
        this.errorCode=errorCode;
        this.errorMsg=errorMsg;
    }


    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public static ErrorEnum findByCode(String code){
        for (ErrorEnum value : ErrorEnum.values()) {
            if(value.getErrorCode().equalsIgnoreCase(code)){
                return value;
            }
        }
        return null;
    }
}
