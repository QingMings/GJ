package com.yhl.gj.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yhl.gj.commons.base.Response;
import com.yhl.gj.service.impl.dto.CustomParam;
import com.yhl.gj.model.Task;
import com.yhl.gj.param.ChooseDetailRequest;
import com.yhl.gj.param.OrderRequest;
import com.yhl.gj.param.TaskQueryRequest;

import java.io.File;
import java.util.List;

public interface TaskService extends IService<Task> {


    Response queryTaskByCondition(TaskQueryRequest request);

    Response chooseDetail(ChooseDetailRequest request);

    List<Task> queryRunningTasks();

    Integer finishedTask(Long id);

    Response<Integer> reTryTaskUseCustomParam(Long taskId, CustomParam param);

    Response<List<File>> listDirAndFiles(String type);

    Response<JSONObject> loadDefaultRunParam();

    Response<Integer> checkTaskIsRunningByPath(OrderRequest request);

    Integer updateTaskName(String satelliteName, Long taskId);

    Response getAllTaskNames();
}

