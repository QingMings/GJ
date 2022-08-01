package com.yhl.gj.controller;

import com.alibaba.fastjson.JSONObject;
import com.yhl.gj.commons.base.Response;
import com.yhl.gj.dto.CustomParam;
import com.yhl.gj.param.ChooseDetailRequest;
import com.yhl.gj.param.TaskDetailsQueryRequest;
import com.yhl.gj.param.TaskQueryRequest;
import com.yhl.gj.service.TaskDetailsService;
import com.yhl.gj.service.TaskService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/orderTask")
public class OrderTaskController {

    @Resource
    private TaskService taskService;

    @Resource
    private TaskDetailsService taskDetailsService;

    /**
     * 任务列表
     */
    @PostMapping("/list")
    private Response listTask(@RequestBody @Valid TaskQueryRequest request) {
        return taskService.queryTaskByCondition(request);
    }

    /**
     * 查看任务详情
     */
    @PostMapping("/details")
    private Response listTaskDetails(@RequestBody @Valid TaskDetailsQueryRequest request) {
        return taskDetailsService.queryTaskDetailsByTaskId(request);
    }

    /**
     * 选定规避策略
     */
    @PostMapping("/chooseDetail")
    private Response chooseDetail(@RequestBody @Valid ChooseDetailRequest request) {
        return taskService.chooseDetail(request);
    }

    /**
     * 查看任务运行结果和当时的运行参数
     */
    @GetMapping("/showRunParamsAndResult/{detailId}")
    private Response<Integer> showTaskDetailRunParamsAndResult(@PathVariable("detailId") Long detailId) {
        return taskDetailsService.showTaskDetailRunParamsAndResult(detailId);
    }

    /**
     * 人工重试一次任务，可自定义参数
     */
    @PostMapping("/toRetryTaskUseCustomParam/{taskId}")
    private Response<Integer> toRetryTaskUseCustomParam(@PathVariable("taskId") Long taskId, @RequestBody CustomParam param) {
        return taskService.reTryTaskUseCustomParam(taskId, param);
    }

    /**
     * 用于给前端列出服务端 指定文件夹路径下的 后缀为 .txt 和 .json 文件
     * 平铺模式，非 tree
     * type:  LEAP(跳秒)、EOP(极移)、SWD(大气环境)、ERR(预报误差参数)
     */
    @GetMapping("/listDirAndFile/{type}")
    private Response<List<File>> listDirAndFiles(@PathVariable("type") String type) {
        return taskService.listDirAndFiles(type);
    }

    @GetMapping("/loadDefaultRunParam")
    private Response<JSONObject> loadDefaultRunParam() {
        return taskService.loadDefaultRunParam();
    }

}
