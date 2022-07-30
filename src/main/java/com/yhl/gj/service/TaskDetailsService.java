package com.yhl.gj.service;

import com.yhl.gj.commons.base.Response;
import com.yhl.gj.dto.LastTaskDetailsDTO;
import com.yhl.gj.model.TaskDetails;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yhl.gj.param.TaskDetailsQueryRequest;

import java.util.List;

public interface TaskDetailsService extends IService<TaskDetails> {


    List<LastTaskDetailsDTO> queryLastTaskDetailByTaskIds(List<Long> taskIds);

    Response queryTaskDetailsByTaskId(TaskDetailsQueryRequest request);

    Response showTaskDetailRunParamsAndResult(Long detailId);
}
