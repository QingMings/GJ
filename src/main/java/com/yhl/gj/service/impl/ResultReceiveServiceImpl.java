package com.yhl.gj.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.id.NanoId;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.yhl.gj.commons.base.Response;
import com.yhl.gj.commons.constant.QueuesConstants;
import com.yhl.gj.component.CallWarningV3Task;
import com.yhl.gj.config.pyconfig.PyCmdParamConfig;
import com.yhl.gj.config.pyconfig.PyV3ParamConfig;
import com.yhl.gj.config.pyconfig.PyV3WorkDirConfig;
import com.yhl.gj.dto.Moves;
import com.yhl.gj.dto.OrderDTO;
import com.yhl.gj.dto.OverAllDTO;
import com.yhl.gj.model.TaskResult;
import com.yhl.gj.param.ResultQueryRequest;
import com.yhl.gj.service.ResultReceiveService;
import com.yhl.gj.service.TaskResultService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.sql.Wrapper;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

import static com.yhl.gj.commons.constant.Constants.after;
import static com.yhl.gj.commons.constant.Constants.before;

@Service
public class ResultReceiveServiceImpl implements ResultReceiveService {

    ThreadFactory callWarningThreadFactory = new CustomizableThreadFactory("call-warningV3-thread-pool-");
    ExecutorService executorService = Executors.newCachedThreadPool(callWarningThreadFactory);

    @Resource
    private org.springframework.core.io.Resource pyV3WorkDir;

    @Resource
    private PyV3WorkDirConfig pyV3WorkDirConfig;
    @Resource
    private PyV3ParamConfig pyCmdParamConfig;

    @Resource
    private TaskResultService taskResultService;
    @Resource
    private RabbitTemplate rabbitTemplate;

    @Override
    public Response onReceiveData(JSONObject receiveData) {
        TaskResult taskResult = createTaskResult(receiveData);
        return Response.buildSucc(taskResult.getId());
    }

    @Override
    public Response manualExecuteTask(OrderDTO orderRequest) {
        Response<File> response =   pyV3WorkDirConfig.writeRunParams(NanoId.randomNanoId(16),JSON.toJSONString(orderRequest));
            if (response.isSuccess()){
                try {
                CallWarningV3Task callWarningV3Task = new CallWarningV3Task(buildCmd(response.getData().getAbsolutePath()),pyV3WorkDir.getFile());
                executorService.submit(callWarningV3Task);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        return Response.buildSucc();
    }

    private String[] buildCmd(String runParamPath) {
        List<String> cmdV3 = pyCmdParamConfig.getCmdV3();
        List<String> cloneCmd = ObjectUtil.clone(cmdV3);
        cloneCmd.add(runParamPath);
        return cloneCmd.toArray(new String[0]);

    }
    @Override
    public Response queryResultByCondition(ResultQueryRequest request) {
        return taskResultService.queryResultByCondition(request);
    }

    /**
     * 将数据保存到DB
     *
     * @param receiveData
     * @return
     */
    private TaskResult createTaskResult(JSONObject receiveData) {
        TaskResult taskResult = new TaskResult();
        OrderDTO orderDTO = receiveData.getObject("order", OrderDTO.class);
        taskResult.setOrderId(orderDTO.getTask().getOrderId());
        taskResult.setTaskStatus(receiveData.getString("task_status"));
        taskResult.setScanInterval(orderDTO.getTask().getScanInterval().longValue());
        taskResult.setSatelliteId(orderDTO.getTask().getSatelliteID());
        taskResult.setCreateDate(DateUtil.date());
        taskResult.setWorkPath(orderDTO.getInput().getPathInputRoot());
        taskResult.setOutputPath(orderDTO.getOutput());
        List<OverAllDTO> overAllDTOS = receiveData.getJSONArray("overall").toJavaList(OverAllDTO.class);
        List<OverAllDTO> overAllDTOSorted = overAllDTOS.stream().sorted(Comparator.comparingInt(OverAllDTO::getLevel)).collect(Collectors.toList());
        // 设置  当前告警 和 告警类型
        if (CollectionUtils.isNotEmpty(overAllDTOSorted)) {
            OverAllDTO overAllDTO = overAllDTOSorted.get(0);
            taskResult.setCurWarnLevel(overAllDTO.getLevel());
            taskResult.setCurWarnType(overAllDTO.getType());
        }
        taskResult.setOrder(JSON.toJSONString(orderDTO));
        taskResult.setChart(readChartData(receiveData).toJSONString());
        taskResult.setStrategy(getStrategy(receiveData).toJSONString());
        updatePidAndOrderId(taskResult);
        taskResultService.saveOrUpdate(taskResult);
        List<OverAllDTO> summary = receiveData.getJSONArray("summary").toJavaList(OverAllDTO.class);
        sendSummaryToMq(summary);
        return taskResult;
    }

    /**
     * 手动：更新pid 和 orderId
     * 自动：设置id
     * @param taskResult
     */
    private void updatePidAndOrderId(TaskResult taskResult) {
        TaskResult taskResultInDB = taskResultService.getOne(Wrappers.lambdaQuery(TaskResult.class).eq(TaskResult::getOrderId, taskResult.getOrderId()));
        if (taskResult.getScanInterval().equals(0L)) {
            // 手动订单
            if (ObjectUtil.isNotNull(taskResultInDB)) {
                taskResult.setPId(taskResultInDB.getId());
                Long count = taskResultService.count(Wrappers.lambdaQuery(TaskResult.class).eq(TaskResult::getPId, taskResultInDB.getId()));
                taskResult.setOrderId(taskResult.getOrderId().concat(String.format("_m%02d", count)));
            }
        }else {
            // 自动订单
            if (ObjectUtil.isNotNull(taskResultInDB)){
                taskResult.setId(taskResultInDB.getId());
            }
        }
    }

    private JSONObject getStrategy(JSONObject receiveData) {
        List<OverAllDTO> overAllDTOS = receiveData.getJSONArray("overall").toJavaList(OverAllDTO.class);
        Moves moves = receiveData.getObject("moves", Moves.class);
        JSONObject strategy = new JSONObject();
        strategy.put("overall", overAllDTOS);
        strategy.put("moves", moves);
        return strategy;
    }

    /**
     * 读取 图标数据
     *
     * @param receiveData
     * @return
     */
    private JSONObject readChartData(JSONObject receiveData) {
        JSONObject orbitEcharts = receiveData.getJSONObject("orbit_echarts");
        String laserEcharts = receiveData.getString("laser_echarts");
        JSONObject chartData = new JSONObject();
        orbitEcharts.forEach((k, v) -> {
            JSONObject pathObject = orbitEcharts.getJSONObject(k);
            if (pathObject.containsKey(before)) {
                contentHandle(pathObject, before);
            }
            if (pathObject.containsKey(after)) {
                contentHandle(pathObject, after);
            }

        });
        chartData.put("orbitEcharts", orbitEcharts.clone());
        chartData.put("laserEcharts", FileUtil.readUtf8String(getAbsolutePath(laserEcharts)));
        return chartData;

    }

    private void contentHandle(JSONObject pathObject, String key) {

        String content = FileUtil.readUtf8String(getAbsolutePath(pathObject.getString(key)));
        JSONObject contentObject = JSON.parseObject(content);
        pathObject.put(key, contentObject);

    }

    private String getAbsolutePath(String path) {
        try {
            return pyV3WorkDir.createRelative(path).getFile().getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将 summary 中的数据发送到 rabbitMq 中
     *
     * @param summary
     */
    private void sendSummaryToMq(List<OverAllDTO> summary) {
        summary.forEach(overAllDTO -> {
            rabbitTemplate.convertAndSend(QueuesConstants.WARN_REPORT_EXCHANGE,
                    QueuesConstants.WARN_REPORT_ROUTE_KEY,
                    JSON.toJSONString(overAllDTO));
        });
    }
}
