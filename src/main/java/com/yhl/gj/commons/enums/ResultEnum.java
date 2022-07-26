package com.yhl.gj.commons.enums;

public enum ResultEnum {
    SUCCESS("200", "操作已成功"),
    ERR500("500", "系统繁忙，请稍后重试"),
    TOKENTIME("401", "登录信息已过期，请重新登录系统"),
    TOKENERR("403", "系统权限不足，请联系管理员"),
    IDANDPASSERROR("405","账号和密码错误，请重试"),
    VERIFICATION_CODE_ERROR("406","验证码错误，请确认验证码内容"),
    LOGIN_FAILED_TIMES_OUT("407","登录失败次数过多，请输入验证码"),
    LOGIN_OUT_SUCCESS("200","当前登录已注销"),
    FILE_NOT_EXISTS("500","文件不存在!"),

    ;


    private String resultCode;
    private String resultMessage;

    ResultEnum(String resultCode, String resultMessage) {
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
    }

    public String getResultCode() {
        return resultCode;
    }

    public String getResultMessage() {
        return resultMessage;
    }
}