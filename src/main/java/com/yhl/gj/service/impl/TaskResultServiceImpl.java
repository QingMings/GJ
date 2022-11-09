package com.yhl.gj.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yhl.gj.commons.base.Response;
import com.yhl.gj.param.ResultQueryRequest;
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
}

