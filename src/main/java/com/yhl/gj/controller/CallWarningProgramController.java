package com.yhl.gj.controller;

import com.yhl.gj.commons.base.Response;
import com.yhl.gj.dto.DataDriverParamRequest;
import com.yhl.gj.dto.UserFaceParamRequest;
import com.yhl.gj.service.CallWarningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
public class CallWarningProgramController {

    @Autowired
    private CallWarningService callWarningService;

    /**
     * 数据驱动模式，传入订单所依赖文件的磁盘路径
     * 程序根据磁盘路径生成订单数据。
     * @param request
     * @return
     */
    @PostMapping("/warning/1")
    public Response dataDriver(@RequestBody @Valid DataDriverParamRequest request){
        return callWarningService.call(request);
    }
    @PostMapping("/warning/2")
    public Response userFace(@RequestBody @Valid UserFaceParamRequest request){
        return callWarningService.call(request);
    }


}
