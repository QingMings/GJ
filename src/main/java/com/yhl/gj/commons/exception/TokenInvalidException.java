package com.yhl.gj.commons.exception;

import org.springframework.http.HttpStatus;

import java.io.Serializable;

public class TokenInvalidException extends RuntimeException implements Serializable {
    private static final long serialVersionUID = 1L;
    private String errorCode;
    private String errorMsg;

    public TokenInvalidException(String errorCode, String errorMsg) {
        super(errorMsg);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public TokenInvalidException(HttpStatus httpStatus){
        this(String.valueOf(httpStatus.value()),httpStatus.getReasonPhrase());
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
