package com.yhl.gj.controller;

import com.yhl.gj.commons.base.Response;
import com.yhl.gj.dto.CustomParam;
import com.yhl.gj.param.ChooseDetailRequest;
import com.yhl.gj.param.TaskDetailsQueryRequest;
import com.yhl.gj.param.TaskQueryRequest;
import com.yhl.gj.service.TaskDetailsService;
import com.yhl.gj.service.TaskService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import javax.annotation.Resource;
import javax.validation.Valid;

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
    private Response listTaskDetails(@RequestBody @Valid TaskDetailsQueryRequest request){
        return taskDetailsService.queryTaskDetailsByTaskId(request);
    }

    /**
     * 选定规避策略
     */
    @PostMapping("/chooseDetail")
    private Response chooseDetail(@RequestBody @Valid ChooseDetailRequest request){
        return taskService.chooseDetail(request);
    }

    @GetMapping("/showRunParamsAndResult/{detailId}")
    private Response showTaskDetailRunParamsAndResult(@PathVariable("detailId") Long detailId){
        return taskDetailsService.showTaskDetailRunParamsAndResult(detailId);
    }
    @PostMapping("/toRetryTaskUseCustomParam/{taskId}")
    private Response toRetryTaskUseCustomParam(@PathVariable("taskId") Long taskId, @RequestBody CustomParam param){
        return taskService.reTryTaskUseCustomParam(taskId,param);
    }
    @GetMapping("/listDirAndFile/{type}")
    private Response listDirAndFiles(@PathVariable("type") String type){
        return taskService.listDirAndFiles(type);
    }
}
