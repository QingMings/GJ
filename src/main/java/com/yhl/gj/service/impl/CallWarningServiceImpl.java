package com.yhl.gj.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yhl.gj.commons.base.Response;
import com.yhl.gj.commons.constant.CallPyModel;
import com.yhl.gj.commons.constant.Constants;
import com.yhl.gj.component.CallWarningProgramTask;
import com.yhl.gj.component.PyLogProcessComponent;
import com.yhl.gj.config.pyconfig.OrderRunParamConfig;
import com.yhl.gj.config.pyconfig.PyCmdParamConfig;
import com.yhl.gj.config.pyconfig.PyInputFileSuffixConfig;
import com.yhl.gj.model.Config;
import com.yhl.gj.model.Task;
import com.yhl.gj.model.TaskDetails;
import com.yhl.gj.param.OrderRequest;
import com.yhl.gj.service.CallWarningService;
import com.yhl.gj.service.ConfigService;
import com.yhl.gj.service.TaskDetailsService;
import com.yhl.gj.service.TaskService;
import com.yhl.gj.task.OrderTaskShared;
import com.yhl.gj.task.OrderTaskTimerTask;
import com.yhl.gj.task.ResumeTaskShared;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.http.util.Asserts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileUrlResource;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

import static cn.hutool.core.date.DatePattern.PURE_DATETIME_FORMAT;
import static com.yhl.gj.commons.constant.Constants.*;

@Slf4j
@Service
public class CallWarningServiceImpl implements CallWarningService {
    ThreadFactory callWarningThreadFactory = new CustomizableThreadFactory("call-warning-thread-pool-");
    ExecutorService executorService = Executors.newCachedThreadPool(callWarningThreadFactory);

    @Resource
    private PyLogProcessComponent pyLogProcessComponent;
    @Resource
    private PyCmdParamConfig pyCmdParamConfig;
    @Resource
    private org.springframework.core.io.Resource pyWork;
    @Resource
    private PyInputFileSuffixConfig inputFileSuffixConfig;
    @Resource
    private TaskService taskService;
    @Resource
    private TaskDetailsService detailsService;
    @Resource
    private ConfigService configService;
    @Resource
    private OrderRunParamConfig runParamConfig;
    @Value("${task.finishedFileFlag}")
    private String taskFinishedFileFlag;
    @Value("${task.period}")
    private Integer taskPeriod;

    @Value("${task.defaultParam.path}")
    private String defaultParamPath;

    /**
     * 执行一次任务生成任务详情(只执行一次)
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Response<Integer> executeTask(Task task, JSONObject param) {
        TaskDetails taskDetails = createTaskDetails(task);
        JSONObject inputFilePaths = loadOrderFiles(task.getOrderPath());
        JSONObject configParam = ObjectUtil.defaultIfNull(param, loadDefaultParams());
        JSONObject order = new JSONObject();
        order.put("inputFileList", inputFilePaths);
        order.put("param", configParam);
        File runParam = saveRunParamToDBAndDisk(order, taskDetails);
        try {
            Asserts.notNull(runParam, "任务运行参数缺失");
            CallWarningProgramTask callWarningProgramTask =
                    createCallWarningProgramTask(runParam.getAbsolutePath(), ObjectUtil.isNull(param));
            executorService.submit(callWarningProgramTask);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        checkTaskFinished(task);
        return Response.buildSuccByDataId(String.valueOf(task.getId()));
    }


    /**
     * 检查是否任务文件夹有 结束标志，如果有，
     * 将数据库任务数据标记为结束
     * 将Timer 取消执行并从map中移除
     */
    private void checkTaskFinished(Task task) {
        boolean checkTaskFinishedFlag = FileUtil.exist(Paths.get(task.getOrderPath()).resolve(taskFinishedFileFlag).toFile());
        if (checkTaskFinishedFlag) {
            // 1. 标记数据库中task 状态 为FINISHED
            taskService.finishedTask(task.getId());
            String taskId = String.valueOf(task.getId());
            Timer resumeTimerTask = ResumeTaskShared.getResumeTaskTimerMap().get(taskId);
            if (ObjectUtil.isNotNull(resumeTimerTask)) {
                // 2.停止Timer
                resumeTimerTask.cancel();
                // 3. 从map中移除taskId
                ResumeTaskShared.getResumeTaskTimerMap().remove(taskId);
                log.info("task:{} is finished", taskId);
            }
            Timer orderTimerTask = OrderTaskShared.getOrderTaskTimerMap().get(taskId);
            if (ObjectUtil.isNotNull(orderTimerTask)) {
                orderTimerTask.cancel();
                OrderTaskShared.getOrderTaskTimerMap().remove(taskId);
                log.info("task:{} is finished", taskId);
            }
        }
    }

    /**
     * 供其他程序通过curl传递参数调用，默认每3s执行一次任务，可通过配置文件配置。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Response<Integer> call(OrderRequest request) {
        Response<Integer> response = taskService.checkTaskIsRunningByPath(request);
        if (!response.isSuccess()) {
            return response;
        }
        // 1. 创建主任务保存
        Task mainTask = createMainTask(request.getOrderPath());
        Timer executeTaskTimer = new Timer();
        OrderTaskShared.getOrderTaskTimerMap().put(String.valueOf(mainTask.getId()), executeTaskTimer);
        OrderTaskTimerTask orderTaskTimerTask = new OrderTaskTimerTask(mainTask, SpringUtil.getBean(CallWarningService.class));
        executeTaskTimer.schedule(orderTaskTimerTask, 0, ObjectUtil.defaultIfNull(taskPeriod, 3000));

        log.info("task:{} is accepted at {}", mainTask.getId(), DateUtil.now());
        return Response.buildSucc();
    }


    /**
     * CallWarningProgramTask Wrapper 一下
     *
     * @param model 运行模式，true 为 数据驱动模式，false是用户传参模式
     */
    private CallWarningProgramTask createCallWarningProgramTask(String runParamPath, boolean model) throws IOException {
        return new CallWarningProgramTask(buildCmd(runParamPath), pyLogProcessComponent, pyWork.getFile(), model ? CallPyModel.DATA_DRIVER : CallPyModel.USER_FACE);
    }

    /**
     * 将任务运行参数写入到数据库中
     */
    private File saveRunParamToDBAndDisk(JSONObject runParam, TaskDetails taskDetails) {
        String content = runParam.toJSONString();
        // 写入磁盘
        Response<File> response = runParamConfig.writeRunParams(buildRunParamName(taskDetails.getTaskId(), taskDetails.getId()), content);

        // 更新 id = taskDetails.getId() 的数据的 runParams 字段
        detailsService.update(
                Wrappers.<TaskDetails>lambdaUpdate()
                        .set(TaskDetails::getRunParams, content)
                        .eq(TaskDetails::getId, taskDetails.getId())
        );
        if (response.isSuccess()) {
            return response.getData();
        } else {
            return null;
        }
    }

    /**
     * 构建任务运行参数的文件名称
     */
    private String buildRunParamName(Long mainTaskId, Long detailId) {
        return String.join("_",
                "任务" + mainTaskId,
                "告警" + detailId, DateUtil.format(DateUtil.date(), PURE_DATETIME_FORMAT));
    }


    private String[] buildCmd(String runParamPath) {
        List<String> cmd = pyCmdParamConfig.getCmd();
        cmd.add(runParamPath);
        return cmd.toArray(new String[0]);
    }


    private Task createMainTask(String orderPath) {
        Task task = new Task(orderPath);
        taskService.save(task);
        return task;
    }

    private TaskDetails createTaskDetails(Task task) {
        TaskDetails taskDetails = new TaskDetails(task.getId(), task.getOrderPath(), task.getTaskName());
        detailsService.save(taskDetails);
        return taskDetails;
    }

    /**
     * 加载任务输入文件路径
     */
    private JSONObject loadOrderFiles(String orderPath) {
        Path path = Paths.get(orderPath);
        IOFileFilter satFileFilter = FileFilterUtils.suffixFileFilter(inputFileSuffixConfig.getSatelliteFile());
        IOFileFilter targetOrbitFilter = FileFilterUtils.suffixFileFilter(inputFileSuffixConfig.getTargetOrbit());
        IOFileFilter targetRadarFilter = FileFilterUtils.suffixFileFilter(inputFileSuffixConfig.getTargetRadar());
        IOFileFilter targetLaserFilter = FileFilterUtils.suffixFileFilter(inputFileSuffixConfig.getTargetLaser());
        IOFileFilter obs_GTW_Filter = FileFilterUtils.suffixFileFilter(inputFileSuffixConfig.getObs_GTW());
        IOFileFilter obs_EPH_Filter = FileFilterUtils.suffixFileFilter(inputFileSuffixConfig.getObs_EPH());

        List<File> satelliteFiles = FileUtil.loopFiles(path.toFile(), satFileFilter);
        List<File> targetOrbitFiles = FileUtil.loopFiles(path.toFile(), targetOrbitFilter);
        List<File> targetRadarFiles = FileUtil.loopFiles(path.toFile(), targetRadarFilter);
        List<File> targetLaserFiles = FileUtil.loopFiles(path.toFile(), targetLaserFilter);
        List<File> obs_GTW_Files = FileUtil.loopFiles(path.toFile(), obs_GTW_Filter);
        List<File> obs_EPH_Files = FileUtil.loopFiles(path.toFile(), obs_EPH_Filter);
        JSONObject inputFileList = new JSONObject();
        // 收集卫星轨道文件路径(最新的一个)
        findLastModifiedFileInPath(inputFileList, satelliteFiles, PATH_SATELLITE);
        // 收集目标文件路径
        collectFilePath(inputFileList, targetOrbitFiles, TARGET_ORBITS);
        // 收集目标雷达路径
        collectFilePath(inputFileList, targetRadarFiles, TARGET_RADARS);
        // 收集目标激光路径
        collectFilePath(inputFileList, targetLaserFiles, TARGET_LASERS);
        // 搜集观测质量评估
        collectFilePath(inputFileList, obs_GTW_Files, OBS_GTW);
        collectFilePath(inputFileList, obs_EPH_Files, OBS_EPH);
        return inputFileList;
    }

    /**
     * 获取默认配置
     */
    public JSONObject loadDefaultParams() {
        Config defaultConfig = configService.getOne(Wrappers.lambdaQuery(Config.class).eq(Config::getIsDefault, Constants.DEFAULT_CONFIG));
        if (ObjectUtil.isNull(defaultConfig)) {
            JSONObject config = flushDefaultConfigToDB();
            return config;
        }
        return JSON.parseObject(defaultConfig.getConfig());
    }

    /**
     * 将配置从文件保存到数据库
     */
    @Override
    public JSONObject flushDefaultConfigToDB() {
        org.springframework.core.io.Resource resource = null;
        String content = null;
        try {
            resource = new FileUrlResource(defaultParamPath);
            content = FileUtil.readUtf8String(resource.getFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Assert.notBlank(content);
        JSONObject config = JSONObject.parseObject(content);
        removeDefaultConfigIfNotNull();
        Config newConfig = createConfig(config);
        configService.saveOrUpdate(newConfig);
        return config;
    }

    /**
     * 移除默认配置
     */
    private void removeDefaultConfigIfNotNull() {
        Config defaultConfig = configService.getOne(Wrappers.lambdaQuery(Config.class).eq(Config::getIsDefault, Constants.DEFAULT_CONFIG));
        if (ObjectUtil.isNotNull(defaultConfig)) {
            configService.removeById(defaultConfig.getId());
        }
    }

    private Config createConfig(JSONObject config) {
        Config newConfig = new Config();
        newConfig.setConfigName("默认配置");
        newConfig.setIsDefault(Constants.DEFAULT_CONFIG);
        newConfig.setConfig(config.toJSONString());
        return newConfig;
    }

    /**
     * 收集路径(将同一文件后缀的文件的路径组成list)
     */
    private void collectFilePath(JSONObject output, List<File> fileSource, String key) {
        if (CollectionUtils.isNotEmpty(fileSource)) {
            JSONArray files = new JSONArray();
            output.put(key, files);
            List<String> filePaths = fileSource.stream().map(File::getPath).collect(Collectors.toList());
            files.addAll(filePaths);
        }
    }

    /**
     * 获得同后缀文件中最后修改时间的文件路径
     */
    private void findLastModifiedFileInPath(JSONObject output, List<File> fileSource, String key) {
        if (CollectionUtils.isNotEmpty(fileSource)) {
            List<File> sortedFiles = CollectionUtil.sort(fileSource, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
            output.put(key, sortedFiles.get(0).getPath());
        }
    }
}
