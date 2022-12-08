package com.yhl.gj.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.id.NanoId;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yhl.gj.commons.base.Response;
import com.yhl.gj.component.CallWarningV3Task;
import com.yhl.gj.config.RabbitConfig;
import com.yhl.gj.config.pyconfig.PyV3ParamConfig;
import com.yhl.gj.config.pyconfig.PyV3WorkDirConfig;
import com.yhl.gj.dto.*;
import com.yhl.gj.model.TaskResult;
import com.yhl.gj.model.WarnResult;
import com.yhl.gj.param.ResultQueryRequest;
import com.yhl.gj.service.ResultReceiveService;
import com.yhl.gj.service.TaskResultService;
import com.yhl.gj.service.WarnResultService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.FileUrlResource;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.yhl.gj.commons.constant.Constants.after;
import static com.yhl.gj.commons.constant.Constants.before;

@Service
public class ResultReceiveServiceImpl implements ResultReceiveService {

    ThreadFactory callWarningThreadFactory = new CustomizableThreadFactory("call-warningV3-thread-pool-");
    ExecutorService executorService = Executors.newCachedThreadPool(callWarningThreadFactory);

    @Resource
    private org.springframework.core.io.Resource pyV3WorkDir;

    @Resource
    private RabbitConfig rabbitConfig;
    @Resource
    private PyV3WorkDirConfig pyV3WorkDirConfig;
    @Resource
    private PyV3ParamConfig pyCmdParamConfig;

    @Resource
    private TaskResultService taskResultService;

    @Resource
    private WarnResultService warnResultService;
    @Resource
    private RabbitTemplate rabbitTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response onReceiveData(JSONObject receiveData) {
        TaskResult taskResult = createTaskResult(receiveData);
        return Response.buildSucc(taskResult.getId());
    }

    @Override
    public Response manualExecuteTask(OrderDTO orderRequest) {
        Response<File> response = pyV3WorkDirConfig.writeRunParams(NanoId.randomNanoId(16), JSON.toJSONString(orderRequest));
        if (response.isSuccess()) {
            try {
                CallWarningV3Task callWarningV3Task = new CallWarningV3Task(buildCmd(response.getData().getAbsolutePath()), pyV3WorkDir.getFile());
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

    @Override
    public Response getOne(Long id) {
        return taskResultService.getOneWithBlobs(id);
    }

    @Override
    public Response listDir(String dir) throws IOException {
        if (StrUtil.isEmpty(dir)) {
            dir = pyV3WorkDirConfig.getTaskDiskRoot();
        }
        Assert.isTrue(dir.startsWith(pyV3WorkDirConfig.getTaskDiskRoot()), "只允许列出 {} 目录下的文件夹", pyV3WorkDirConfig.getTaskDiskRoot());
        try (Stream<Path> stream = Files.list(Paths.get(dir))) {
            Set<String> collect = stream
                    .filter(Files::isDirectory)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toSet());
            JSONObject output = new JSONObject();
            output.put("path", dir);
            output.put("childDirs", collect);
            return Response.buildSucc(output);
        }
    }

    @Override
    public Response getSatellites() {
        return taskResultService.getSatellites();
    }

    @Override
    public Response getDefaultParam() {
        try {
            org.springframework.core.io.Resource resource = new FileUrlResource(pyCmdParamConfig.getDefaultParamPath());
            String content = FileUtil.readUtf8String(resource.getFile());
            return Response.buildSucc(JSON.parseObject(content));
        } catch (IOException e) {

            return Response.buildFail(500, "读取默认参数配置失败");
        }
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
        overAllDTOS.forEach(overAllDTO -> {
            if ("orbit".equals(overAllDTO.getType())) {
                taskResult.setOrbitWarnLevel(overAllDTO.getLevel());
            } else if ("laser".equals(overAllDTO.getType())) {
                taskResult.setLaserWarnLevel(overAllDTO.getLevel());
            }
        });
        Alarms alarms = receiveData.getObject("alarms", Alarms.class);
        taskResult.setAlarms(JSON.toJSONString(alarms));
        taskResult.setOrder(JSON.toJSONString(orderDTO));
        taskResult.setChart(readChartData(receiveData).toJSONString());
        taskResult.setStrategy(getStrategy(receiveData).toJSONString());
        taskResult.setPathGbclXml(getPathGbclXml(receiveData));
        updatePidAndOrderId(taskResult);
        taskResultService.saveOrUpdate(taskResult);
        List<OverAllDTO> summary = receiveData.getJSONArray("summary").toJavaList(OverAllDTO.class);
        // 发送warn到 mq
        sendSummaryToMq(summary);
        // 保存 warnResults
        saveWarnResultBatch(taskResult, summary);
        return taskResult;
    }

    private void saveWarnResultBatch(TaskResult taskResult, List<OverAllDTO> summary) {
        List<WarnResult> warnResults = Collections.unmodifiableList(summary.stream().map(overAllDTO -> {
            WarnResult warnResult = new WarnResult();
            warnResult.setTaskId(taskResult.getId());
            warnResult.setOrderId(taskResult.getOrderId());
            warnResult.setSatelliteId(taskResult.getSatelliteId());
            warnResult.setWarnLevel(overAllDTO.getLevel());
            warnResult.setWarnInfo(overAllDTO.getDetail());
            warnResult.setTargetId(overAllDTO.getTargetId());
            warnResult.setWarnStatus(overAllDTO.getLevel() > 0 ? 0 : 1);
            warnResult.setWarnType(overAllDTO.getType());
            if (StrUtil.isNotEmpty(overAllDTO.getUtc())) {
                warnResult.setWarnTimeUtc(DateUtil.parse(overAllDTO.getUtc()));
            }
            return warnResult;
        }).collect(Collectors.toList()));
        warnResultService.saveBatch(warnResults);
    }

    private String getPathGbclXml(JSONObject receiveData) {
        try {
            String xmlPath = getAbsolutePath(receiveData.getString("path_gbcl_xml"));
            String content = StrUtil.isEmpty(xmlPath) ? "" : FileUtil.readUtf8String(xmlPath);
            if (StrUtil.isNotEmpty(content)) {
                return content;
            }
        } catch (Exception e) {

        }
        return "";
    }

    /**
     * 手动：更新pid 和 orderId
     * 自动：设置id
     *
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
        } else {
            // 自动订单 设置id 和 updateDate
            if (ObjectUtil.isNotNull(taskResultInDB)) {
                taskResult.setId(taskResultInDB.getId());
                taskResult.setCreateDate(taskResultInDB.getCreateDate());
                taskResult.setUpdateDate(DateUtil.date());
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

        String laserEchartsData = StrUtil.isEmpty(laserEcharts) ? "" : FileUtil.readUtf8String(getAbsolutePath(laserEcharts));
        chartData.put("laserEcharts", JSON.parseObject(laserEchartsData));
        return chartData;

    }

    private void contentHandle(JSONObject pathObject, String key) {

        String content = StrUtil.isEmpty(pathObject.getString(key)) ? "" : FileUtil.readUtf8String(getAbsolutePath(pathObject.getString(key)));
        JSONObject contentObject = JSON.parseObject(content);
        pathObject.put(key, contentObject);

    }

    private String getAbsolutePath(String path) {
        try {
            if (StrUtil.startWith(path, ".")) {
                return pyV3WorkDir.createRelative(path).getFile().getAbsolutePath();
            } else {
                return path;
            }

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
            rabbitTemplate.convertAndSend(rabbitConfig.getGjWarnExchange(),
                    rabbitConfig.getGjWarnRouteKey(),
                    JSON.toJSONString(overAllDTO));
        });
    }

    /**
     * 导出 txt 或者 csv
     * * 前端库 对应
     * * txt https://www.npmjs.com/package/json-to-txt
     * * csv https://www.npmjs.com/package/json-to-csv-export
     *
     * @param id
     * @param type 类型   txt  or csv
     * @return
     */
    @Override
    public Response getMovesToFiles(Long id, String type) {
        StrategyDTO strategyDTO = this.taskResultService.getMovesById(id);
        if (ObjectUtil.isNotNull(strategyDTO)) {
            Moves moves = strategyDTO.getMoves();
            List<Moves.MovesDTO> movesDTOS = moves.getMoves();
            if (CollectionUtils.isNotEmpty(movesDTOS)) {
                JSONArray result = new JSONArray();
                for (int i = 0; i < movesDTOS.size(); i++) {
                    Moves.MovesDTO movesDTO = movesDTOS.get(i);
                    JSONObject output = new JSONObject(true);
                    if (StrUtil.equals("txt", type)) {
                        output.put("C", "");
                    }
                    output.put("序号", i);
                    output.put("UTC时间", movesDTO.getUtc());
                    output.put("速度增量dVx[m/s]", movesDTO.getVecdvXyz().get(0));
                    output.put("速度增量dVy[m/s]", movesDTO.getVecdvXyz().get(1));
                    output.put("速度增量dVz[m/s]", movesDTO.getVecdvXyz().get(2));
                    output.put("本星位置Rx[km]", movesDTO.getVecr().get(0));
                    output.put("本星位置Ry[km]", movesDTO.getVecr().get(1));
                    output.put("本星位置Rz[km]", movesDTO.getVecr().get(2));
                    output.put("本星速度Vx[m/s]", movesDTO.getVecvMinus().get(0));
                    output.put("本星速度Vy[m/s]", movesDTO.getVecvMinus().get(1));
                    output.put("本星速度Vz[m/s]", movesDTO.getVecvMinus().get(2));
                    result.add(output);
                }
                return Response.buildSucc(result);
            }
        }
        return Response.buildFail(500, "未找到变轨数据");
    }

    @Override
    public Response sendXmlToMq(Long id) {
        String xmlContent = taskResultService.getPathGbclXml(id);
        if (StrUtil.isNotEmpty(xmlContent)) {
            rabbitTemplate.convertAndSend(rabbitConfig.getGjXmlExchange(),
                    rabbitConfig.getGjXmlRouteKey(),
                    xmlContent);
            return Response.buildSucc();
        } else {
            return Response.buildFail(500, "xml 内容为空");
        }

    }
}
