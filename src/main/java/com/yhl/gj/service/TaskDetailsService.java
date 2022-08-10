package com.yhl.gj.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yhl.gj.commons.base.Response;
import com.yhl.gj.dto.LastTaskDetailsDTO;
import com.yhl.gj.model.TaskDetails;
import com.yhl.gj.param.TaskDetailsQueryRequest;
import com.yhl.gj.vo.TaskDetailsVO;

import java.util.List;

public interface TaskDetailsService extends IService<TaskDetails> {


    List<LastTaskDetailsDTO> queryLastTaskDetailByTaskIds(List<Long> taskIds);

    Response queryTaskDetailsByTaskId(TaskDetailsQueryRequest request);

    Response<TaskDetailsVO> showTaskDetailRunParamsAndResult(Long detailId);

    TaskDetails findLastTaskDetails(Long id);

    Integer updateTaskName(String satelliteName, Long id);
}


