package com.yhl.gj.commons.base;


import com.yhl.gj.commons.enums.CommonError;
import com.yhl.gj.commons.enums.ResultEnum;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.io.Serializable;
import java.util.stream.Collectors;

@Data
public class Response<T> implements Serializable {

    /**
     * 是否成功 <br/>
     * true:成功 false:失败
     */
    private boolean isSuccess = false;
    /**
     * 返回数据
     */
    private T data;
    /**
     * 错误码
     */
    private String errorCode;
    /**
     * 错误描述
     */
    private String errorMessage;
    /**
     * 数据id 操作记录查询条件
     */
    private String dataId;

    public Response() {
        this(false);
    }


    public Response(HttpStatus httpStatus) {
        this.setSuccess(false);
        this.setErrorCode(String.valueOf(httpStatus.value()));
        this.setErrorMessage(httpStatus.getReasonPhrase());
    }


    public Response(CommonError commonError) {
        this.setSuccess(false);
        this.setErrorCode(String.valueOf(commonError.getCode()));
        this.setErrorMessage(commonError.getMessage());
    }



    public Response(BindingResult bindingResult) {
        this.setSuccess(false);
        this.setErrorCode("500");
        this.setErrorMessage(bindingResult.getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.toList()).toString());
    }


    public Response(boolean success) {
        this(success, null);
    }

    public Response(boolean success, T data) {
        this.setSuccess(success);
        this.data = data;
    }

    public Response(String resultCode, String resultMessage) {
        this(false);
        this.errorCode = resultCode;
        this.errorMessage = resultMessage;
    }

    private Response(ResultEnum resultCode, T body) {
        this.setSuccess(true);
        this.setErrorCode(resultCode.getResultCode());
        this.setErrorMessage(resultCode.getResultMessage());
        this.data = body;
    }

    private Response(ResultEnum resultCode) {
        this.setSuccess(false);
        this.setErrorCode(resultCode.getResultCode());
        this.setErrorMessage(resultCode.getResultMessage());
    }

    public Response(T data) {
        this(true, data);
    }

    public static <T> Response<T> buildSucc(T result) {
        return new Response<T>(ResultEnum.SUCCESS, result);
    }

    public static <T> Response<T> buildSuccByDataId(String result) {
        Response<T> tResponse = new Response<T>();
        tResponse.setSuccess(true);
        tResponse.setErrorCode(ResultEnum.SUCCESS.getResultCode());
        tResponse.setErrorMessage(ResultEnum.SUCCESS.getResultMessage());
        tResponse.setDataId(result);
        return tResponse;
    }

    public static <T> Response<T> buildSucc(String code, String message) {
        Response<T> response = new Response<T>();
        response.setSuccess(true);
        response.setErrorCode(code);
        response.setErrorMessage(message);
        return response;
    }

    public static <T> Response<T> buildSucc() {
        return new Response<T>(ResultEnum.SUCCESS, null);
    }

    public static <T> Response<T> buildFail(String code, String message) {
        return new Response<T>(code, message);
    }

    public static <T> Response<T> buildFail(ResultEnum resultCode) {
        return new Response<T>(resultCode);
    }


   }