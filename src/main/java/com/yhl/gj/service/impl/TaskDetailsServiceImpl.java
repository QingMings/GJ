package com.yhl.gj.service.impl;

import com.alibaba.fastjson.JSON;
import com.yhl.gj.commons.base.Response;
import com.yhl.gj.dto.LastTaskDetailsDTO;
import com.yhl.gj.param.TaskDetailsQueryRequest;
import com.yhl.gj.vo.TaskDetailsVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yhl.gj.model.TaskDetails;
import com.yhl.gj.mapper.TaskDetailsMapper;
import com.yhl.gj.service.TaskDetailsService;

@Service
public class TaskDetailsServiceImpl extends ServiceImpl<TaskDetailsMapper, TaskDetails> implements TaskDetailsService {
    /**
     * 根据任务ID查找最新的任务详情ID
     *
     * @param taskIds
     * @return
     */
    @Override
    public List<LastTaskDetailsDTO> queryLastTaskDetailByTaskIds(List<Long> taskIds) {

        return baseMapper.queryListTaskDetailByTaskIds(taskIds);
    }

    /**
     * 根据任务id,查询任务详情列表
     */
    @Override
    public Response queryTaskDetailsByTaskId(TaskDetailsQueryRequest request) {
        List<TaskDetailsVO> taskDetailsVOList = baseMapper.queryTaskDetailsByTaskId(request);
        // 将json字符串转json对象
        taskDetailsVOList.forEach(t -> {
            t.setStrategy(JSON.parseObject(t.getStrategyStr()));
        });
        return Response.buildSucc(taskDetailsVOList);
    }

    /**
     * 查看任务详情的运行参数和运行结果数据
     */
    @Override
    public Response showTaskDetailRunParamsAndResult(Long detailId) {
        return null;
    }
}

