package com.yhl.gj.controller;

import com.yhl.gj.commons.base.Response;
import com.yhl.gj.param.UserLoginRequest;
import com.yhl.gj.service.SystemService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 临时登录接口，等后续确定登录集成方式后再搞
 */
@RestController
@RequestMapping("/sys")
public class SystemController {

    @Resource
    private SystemService systemService;
    @PostMapping("/userLogin")
    public Response userLogin(@RequestBody @Valid UserLoginRequest request) {

        String uid = "gj_user_01";
        return Response.buildSucc(uid);
//        return systemService.doLogin(request);
    }


    @PostMapping("/loginOut")
    public Response logOut(@RequestParam("token") String token) {

        return systemService.logOut(token);
    }

}
