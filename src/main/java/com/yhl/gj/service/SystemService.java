package com.yhl.gj.service;

import com.yhl.gj.commons.base.Response;
import com.yhl.gj.param.UserLoginRequest;

public interface SystemService {
    Response doLogin(UserLoginRequest request);

    Response logOut(String token);
}
