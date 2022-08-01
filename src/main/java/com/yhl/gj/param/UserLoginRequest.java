package com.yhl.gj.param;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UserLoginRequest {

    @NotNull(message = "userName 必填")
    private String userName;
    @NotNull(message = "password 必填")
    private String password;

}
