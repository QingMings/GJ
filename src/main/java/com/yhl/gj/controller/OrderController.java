package com.yhl.gj.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yhl.gj.commons.base.Response;
import com.yhl.gj.param.OrderRequest;
import com.yhl.gj.service.CallWarningService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

//@RestController
//@RequestMapping("/api")
public class OrderController {

    @Resource
    private CallWarningService callWarningService;

    /**
     * ，传入订单所依赖文件的磁盘路径
     * 程序根据磁盘路径生成订单数据。
     */
    @PostMapping("/executeTask")
    public Response callFromCURL(@RequestBody @Valid OrderRequest request) {
        return callWarningService.call(request);
    }



}
