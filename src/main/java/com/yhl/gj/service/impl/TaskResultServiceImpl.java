package com.yhl.gj.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yhl.gj.commons.base.Response;
import com.yhl.gj.dto.OrderDTO;
import com.yhl.gj.dto.StrategyDTO;
import com.yhl.gj.param.ResultQueryRequest;
import com.yhl.gj.vo.TaskResultVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yhl.gj.mapper.TaskResultMapper;
import com.yhl.gj.model.TaskResult;
import com.yhl.gj.service.TaskResultService;

@Service
public class TaskResultServiceImpl extends ServiceImpl<TaskResultMapper, TaskResult> implements TaskResultService {


    @Override
    public Response queryResultByCondition(ResultQueryRequest request) {
        PageHelper.startPage(request.getCurrentPage(), request.getPageSize());
        List<TaskResult> taskResults = this.baseMapper.queryTaskResultByCondition(request);
        PageInfo<TaskResult> pageInfo = PageInfo.of(taskResults);
        return Response.buildSucc(pageInfo);
    }

    @Override
    public Response getOneWithBlobs(Long id) {
        TaskResult taskResult = this.baseMapper.getOneWithBlobs(id);
        TaskResultVO taskResultVO = convertToVo(taskResult);
        return Response.buildSucc(taskResultVO);
    }

    private TaskResultVO convertToVo(TaskResult taskResult) {
        TaskResultVO taskResultVO = new TaskResultVO();
        BeanUtils.copyProperties(taskResult, taskResultVO, "order", "strategy", "chart");
        taskResultVO.setChart(JSON.parseObject(taskResult.getChart()));
        taskResultVO.setOrder(JSON.parseObject(taskResult.getOrder()).toJavaObject(OrderDTO.class));
        taskResultVO.setStrategy(JSON.parseObject(taskResult.getStrategy()).toJavaObject(StrategyDTO.class));
        return taskResultVO;
    }

    @Override
    public Response getSatellites() {
        List<String> satellites = this.baseMapper.getSatellites();
        return Response.buildSucc(satellites);
    }
}


