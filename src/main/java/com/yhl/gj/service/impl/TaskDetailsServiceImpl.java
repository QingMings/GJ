package com.yhl.gj.service.impl;

import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yhl.gj.commons.base.Response;
import com.yhl.gj.dto.LastTaskDetailsDTO;
import com.yhl.gj.mapper.TaskDetailsMapper;
import com.yhl.gj.model.TaskDetails;
import com.yhl.gj.param.TaskDetailsQueryRequest;
import com.yhl.gj.service.CallWarningService;
import com.yhl.gj.service.TaskDetailsService;
import com.yhl.gj.vo.TaskDetailsVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import static com.yhl.gj.commons.constant.Constants.*;

//@Service
public class TaskDetailsServiceImpl extends ServiceImpl<TaskDetailsMapper, TaskDetails> implements TaskDetailsService {

    @Resource
    private CallWarningService callWarningService;
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
        PageHelper.startPage(request.getCurrentPage(), request.getPageSize());
        List<TaskDetailsVO> taskDetailsVOList = baseMapper.queryTaskDetailsByTaskId(request);

        // 将json字符串转json对象
//        taskDetailsVOList.forEach(t -> t.setStrategy(JSON.parseObject(t.getStrategyStr())));
        PageInfo<TaskDetailsVO> pageInfo = PageInfo.of(taskDetailsVOList);
        return Response.buildSucc(pageInfo);
    }

    /**
     * 查看任务详情的运行参数和运行结果数据
     */
    @Override
    public Response<TaskDetailsVO> showTaskDetailRunParamsAndResult(Long detailId) {
        TaskDetails taskDetails = getById(detailId);
        Assert.notNull(taskDetails,"根据 detailId:{} 未找到数据",detailId);
        TaskDetailsVO taskDetailsVO = new TaskDetailsVO();
        convertToVO(taskDetailsVO,taskDetails);
        return Response.buildSucc(taskDetailsVO);
    }

    /**
     * 转换vo
     */
    private void  convertToVO(TaskDetailsVO taskDetailsVO,TaskDetails taskDetails){
        taskDetailsVO.setId(taskDetails.getId());
        taskDetailsVO.setTaskId(taskDetails.getTaskId());
        taskDetailsVO.setTaskName(taskDetails.getTaskName());
        taskDetailsVO.setTaskType(taskDetails.getTaskType());
        taskDetailsVO.setMenaceSource(taskDetails.getMenaceSource());
        taskDetailsVO.setCreateTime(taskDetails.getCreateTime());
        taskDetailsVO.setOrderPath(taskDetails.getOrderPath());
        taskDetailsVO.setWarnLevel(taskDetails.getWarnLevel());
        JSONObject defaultParam = callWarningService.loadDefaultParams();
        JSONObject runParams = JSON.parseObject(taskDetails.getRunParams()).getJSONObject(params);
        runParams.put(path_leap_default_flag,isDefaultConfig(path_leap,defaultParam,runParams));
        runParams.put(path_eop_default_flag,isDefaultConfig(path_eop,defaultParam,runParams));
        runParams.put(path_swd_default_flag,isDefaultConfig(path_swd,defaultParam,runParams));
        runParams.put(path_error_default_flag, isDefaultConfig(path_error, defaultParam, runParams));
        taskDetailsVO.setRunParams(runParams);
        taskDetailsVO.setStrategy(JSON.parseObject(taskDetails.getStrategy()));
    }

    /**
     * 是否是默认配置项
     */
    private boolean  isDefaultConfig(String key,JSONObject defaultObject,JSONObject runParam){
        return  defaultObject.getString(key).equals(runParam.getString(key));
    }

    /**
     * 根据任务ID，查找最后一次的任务详情
     */
    @Override
    public TaskDetails findLastTaskDetails(Long id) {
        return baseMapper.selectLastTaskDetails(id);
    }

    @Override
    public Integer updateTaskName(String satelliteName, Long id) {
        return baseMapper.updateTaskName(satelliteName, id);
    }
}





