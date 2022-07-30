package com.yhl.gj.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yhl.gj.commons.base.Response;
import com.yhl.gj.dto.LastTaskDetailsDTO;
import com.yhl.gj.model.TaskDetails;
import com.yhl.gj.param.ChooseDetailRequest;
import com.yhl.gj.service.TaskDetailsService;
import com.yhl.gj.param.TaskQueryRequest;
import com.yhl.gj.vo.TaskVO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yhl.gj.model.Task;
import com.yhl.gj.mapper.TaskMapper;
import com.yhl.gj.service.TaskService;

@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {

    /**
     * 将task标记为完成
     *
     * @return
     */
    @Override
    public Integer finishedTask(Long id) {

        return  baseMapper.finishedTask(id);
    }

    /**
     * 查询在运行状态的任务
     * @return
     */
    public List<Task> queryRunningTasks() {
        return baseMapper.queryRunningTasks();
    }

    /**
     * 选择任务详情
     * @param request
     * @return
     */
    @Override
    public Response chooseDetail(ChooseDetailRequest request) {
        Response response = new Response();
        TaskDetails taskDetails = taskDetailsService.getById(request.getDetailId());
        Task task = getById(request.getTaskId());
        if (ObjectUtil.isNull(taskDetails) || ObjectUtil.isNull(task)){
            response.setCode(500);
            response.setMessage("根据taskId或taskDetailId,没有查到对应数据");
            return response;
        }
        task.setCheckedDetailId(taskDetails.getId());
        updateById(task);
        return Response.buildSucc();
    }

    @Resource
    private TaskDetailsService taskDetailsService;
    /**
     * 分页查询任务列表
     *
     * @param request 查询参数
     * @return Response
     */
    @Override
    public Response<PageInfo<TaskVO>> queryTaskByCondition(TaskQueryRequest request) {
        PageHelper.startPage(request.getCurrentPage(),request.getPageSize());
        List<TaskVO> taskVOS = this.baseMapper.queryTaskByCondition(request);
        if (CollectionUtils.isNotEmpty(taskVOS)){
            fillLastDetailIdToTask(taskVOS);
        }
        PageInfo<TaskVO> pageInfo = PageInfo.of(taskVOS);
        return Response.buildSucc(pageInfo);
    }

    private void fillLastDetailIdToTask(List<TaskVO> taskVOS){
        List<Long> taskIds = taskVOS.stream().map(TaskVO::getId).collect(Collectors.toList());
        List<LastTaskDetailsDTO> lastTaskDetailsDTOList = taskDetailsService.queryLastTaskDetailByTaskIds(taskIds);
        Map<Long,LastTaskDetailsDTO> lastTaskDetailsDTOMap= lastTaskDetailsDTOList.stream().collect(Collectors.toMap(LastTaskDetailsDTO::getTaskId, it -> it));
        taskVOS.forEach(taskVO -> {
            LastTaskDetailsDTO lastTaskDetailsDTO= lastTaskDetailsDTOMap.get(taskVO.getId());
            taskVO.setLastDetailId(lastTaskDetailsDTO.getId());
            taskVO.setUpdateDate(lastTaskDetailsDTO.getCreateTime());
        });
    }
}

