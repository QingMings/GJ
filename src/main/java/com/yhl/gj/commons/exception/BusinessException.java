package com.yhl.gj.commons.exception;



import com.yhl.gj.commons.enums.ResultEnum;

import java.io.Serializable;

public class BusinessException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer errorCode;
    private String errorMsg;

    public BusinessException(Integer errorCode, String errorMsg) {
        super(errorMsg);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public BusinessException(ResultEnum error){
        super(error.getResultMessage());
        this.errorCode = error.getResultCode();
        this.errorMsg = error.getResultMessage();
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