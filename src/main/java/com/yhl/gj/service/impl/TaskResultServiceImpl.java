package com.yhl.gj.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yhl.gj.commons.base.Response;
import com.yhl.gj.dto.OrderDTO;
import com.yhl.gj.dto.StrategyDTO;
import com.yhl.gj.model.Task;
import com.yhl.gj.param.ResultQueryRequest;
import com.yhl.gj.vo.TaskResultVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
        List<TaskResultVO> collect = taskResults.stream().map(this::convertToVo).collect(Collectors.toList());
        PageInfo<TaskResult> pageInfoPo = PageInfo.of(taskResults);
        PageInfo<TaskResultVO> pageInfoVo = new PageInfo<>();
        BeanUtils.copyProperties(pageInfoPo,pageInfoVo);
        pageInfoVo.setList(collect);
        return Response.buildSucc(pageInfoVo);
    }

    @Override
    public Response getOneWithBlobs(Long id) {
        TaskResult taskResult = this.baseMapper.getOneWithBlobs(id);
        TaskResultVO taskResultVO = convertToVo(taskResult);
        return Response.buildSucc(taskResultVO);
    }

    private TaskResultVO convertToVo(TaskResult taskResult) {
        TaskResultVO taskResultVO = new TaskResultVO();
        BeanUtils.copyProperties(taskResult, taskResultVO, "order", "strategy", "chart", "alarms");
        if (StrUtil.isNotEmpty(taskResult.getChart())) {
            taskResultVO.setChart(JSON.parseObject(taskResult.getChart()));
        }
        if (StrUtil.isNotEmpty(taskResult.getOrder())) {
            taskResultVO.setOrder(JSON.parseObject(taskResult.getOrder()).toJavaObject(OrderDTO.class));
        }
        if (StrUtil.isNotEmpty(taskResult.getStrategy())) {
            taskResultVO.setStrategy(JSON.parseObject(taskResult.getStrategy()).toJavaObject(StrategyDTO.class));
        }
        if (StrUtil.isNotEmpty(taskResult.getAlarms())) {
            taskResultVO.setAlarms(sortedAlarms(JSON.parseObject(taskResult.getAlarms())));
        }
        if (ObjectUtil.isNull(taskResult.getUpdateDate())){
            taskResultVO.setUpdateDate(taskResult.getCreateDate());
        }
        return taskResultVO;
    }
    private JSONObject sortedAlarms(JSONObject oldAlarms){
        JSONArray  laser = oldAlarms.getJSONArray("laser");
        JSONArray  orbit = oldAlarms.getJSONArray("orbit");
        Collections.reverse(laser);
//        Collections.reverse(orbit);
        return oldAlarms;
    }

    @Override
    public Response getSatellites() {
        List<String> satellites = this.baseMapper.getSatellites();
        return Response.buildSucc(satellites);
    }

    @Override
    public StrategyDTO getMovesById(Long id) {
        String movesStr = this.baseMapper.getMovesById(id);
        if (StrUtil.isNotEmpty(movesStr)) {
            return JSON.parseObject(movesStr, StrategyDTO.class);
        }
        return null;
    }

    @Override
    public String getPathGbclXml(Long id) {
        return this.baseMapper.getPathGbclXml(id);
    }
}




