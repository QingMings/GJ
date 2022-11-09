package com.yhl.gj.controller;

import com.alibaba.fastjson.JSONObject;
import com.yhl.gj.commons.base.Response;
import com.yhl.gj.dto.OrderDTO;
import com.yhl.gj.param.ResultQueryRequest;
import com.yhl.gj.service.ResultReceiveService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 2022-11-08  新的变更单独放出来了
 */
@RestController
@RequestMapping("/api_v2")
public class ResultReceiveController {

    @Resource
    private ResultReceiveService resultReceiveService;
    /**
     * new   python 推送数据接口
     * @param request
     * @return
     */
    @PostMapping("/result")
    public Response resultPost(@RequestBody String  request) {
        JSONObject requestData = JSONObject.parseObject(request);
        JSONObject result = requestData.getJSONObject("result");

        return resultReceiveService.onReceiveData(result);
    }

    /**
     * 手动执行任务
     * @param orderRequest
     * @return
     */
    @PostMapping("/manualTask")
    public Response manualExecuteTask(@RequestBody OrderDTO orderRequest){
        return resultReceiveService.manualExecuteTask(orderRequest);
    }

    @PostMapping("/list")
    public Response resultList(@RequestBody @Valid ResultQueryRequest request){
        return resultReceiveService.queryResultByCondition(request);
    }
}
