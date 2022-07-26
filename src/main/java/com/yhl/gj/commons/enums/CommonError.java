package com.yhl.gj.commons.enums;

public enum CommonError {
    ILLEGAL_REQUEST(500,"非法请求"),
    PARAMETER_ERROR(500,"参数错误"),
    ;

    private int code;

    private String message;

    CommonError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}