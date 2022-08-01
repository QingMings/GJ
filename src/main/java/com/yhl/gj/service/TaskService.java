package com.yhl.gj.service;

import com.yhl.gj.commons.base.Response;
import com.yhl.gj.dto.CustomParam;
import com.yhl.gj.model.Task;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yhl.gj.param.ChooseDetailRequest;
import com.yhl.gj.param.TaskQueryRequest;

import java.util.List;

public interface TaskService extends IService<Task> {


    Response queryTaskByCondition(TaskQueryRequest request);

    Response chooseDetail(ChooseDetailRequest request);

    List<Task> queryRunningTasks();

    Integer finishedTask(Long id);

    Response reTryTaskUseCustomParam(Long taskId, CustomParam param);

    Response listDirAndFiles(String type);
}

