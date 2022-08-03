package com.yhl.gj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yhl.gj.model.Task;
import com.yhl.gj.param.TaskQueryRequest;
import com.yhl.gj.vo.TaskVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {
    List<TaskVO> queryTaskByCondition(@Param("param") TaskQueryRequest request);

    List<Task> queryRunningTasks();

    Integer finishedTask(@Param("id") Long id);

    Integer updateTaskName(@Param("satelliteName") String satelliteName, @Param("taskId") Long taskId);
}