package com.yhl.gj.service.impl;

import cn.hutool.crypto.SecureUtil;
import com.yhl.gj.commons.base.Response;
import com.yhl.gj.param.UserLoginRequest;
import com.yhl.gj.service.CommonBaseService;
import com.yhl.gj.service.SystemService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
@Service
public class SystemServiceImpl implements SystemService {
    @Resource
    private CommonBaseService commonBaseService;
    @Override
    public Response doLogin(UserLoginRequest request) {

        Response response = commonBaseService.userLogin(request.getUserName(),SecureUtil.md5(request.getPassword()));

        return response;
    }

    @Override
    public Response logOut(String token) {
        Response response = commonBaseService.userLogout(token);
        return response;
    }
}
