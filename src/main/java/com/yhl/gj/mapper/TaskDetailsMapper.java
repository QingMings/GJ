package com.yhl.gj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yhl.gj.dto.LastTaskDetailsDTO;
import com.yhl.gj.model.TaskDetails;
import com.yhl.gj.param.TaskDetailsQueryRequest;
import com.yhl.gj.vo.TaskDetailsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskDetailsMapper extends BaseMapper<TaskDetails> {
    List<LastTaskDetailsDTO> queryListTaskDetailByTaskIds(@Param("taskIds") List<Long> taskIds);

    List<TaskDetailsVO> queryTaskDetailsByTaskId(@Param("request") TaskDetailsQueryRequest request);

    TaskDetails selectLastTaskDetails(@Param("id") Long id);

    Integer updateTaskName(@Param("satelliteName") String satelliteName, @Param("id") Long id);
}