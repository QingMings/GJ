package com.yhl.gj.commons.exception;

import org.springframework.http.HttpStatus;

import java.io.Serializable;

public class TokenInvalidException extends RuntimeException implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer errorCode;
    private String errorMsg;

    public TokenInvalidException(Integer errorCode, String errorMsg) {
        super(errorMsg);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public TokenInvalidException(HttpStatus httpStatus){
        this(httpStatus.value(),httpStatus.getReasonPhrase());
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
