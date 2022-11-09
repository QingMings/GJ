package com.yhl.gj;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.ObjectUtil;
import com.yhl.gj.model.Task;
import com.yhl.gj.service.CallWarningService;
import com.yhl.gj.service.TaskDetailsService;
import com.yhl.gj.service.TaskService;
import com.yhl.gj.task.ResumeTaskShared;
import com.yhl.gj.task.ResumeTaskTimerTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import static com.yhl.gj.commons.constant.Constants.FINISHED;

/**
 * 用于恢复程序异常退出后停止执行的任务
 */
@Slf4j
//@Component
public class RunningStatusTaskResume implements CommandLineRunner {

    @Value("${task.enableResumeTask}")
    private boolean enableResume;
    @Value("${task.enableOrderTask}")
    private Boolean enableOrderTask;
    @Value("${pyScript.usePoolVersion}")
    private boolean usePoolVersion;
    @Value("${task.finishedFileFlag}")
    private String taskFinishedFileFlag;
    @Value("${task.period}")
    private Integer taskPeriod;
    @Resource
    private TaskService taskService;
    @Resource
    private TaskDetailsService taskDetailsService;
    @Resource
    private CallWarningService callWarningService;
    @Resource
    private Environment environment;

    @Override
    public void run(String... args)  {
        started();
        if (!ObjectUtil.defaultIfNull(enableResume,true)) {
            log.info("------任务恢复已暂停，不会恢复数据库中标志为运行中的任务-----");
            return;
        }
        List<Task> needResumeTasks = taskService.queryRunningTasks();
        checkTaskIsFinished(needResumeTasks);
        resumeTasks(needResumeTasks);

    }
    private void started(){
        log.info("------程序启动成功------");

        log.info("------IP: "+ NetUtil.getLocalhost().getHostAddress());
        log.info("------PORT: "+environment.getProperty("server.port"));
        log.info("------任务结束文件标志：{}------", taskFinishedFileFlag);
        log.info("------任务运行时间间隔(ms)：{}-----",taskPeriod);
        log.info("------任务运行模式为:{}",enableOrderTask? "OrderTask模式(Timer定时执行任务，直到收到任务结束标志)":"RabbitMQ 触发模式（触发一次，执行一次任务）" );
        log.info("------py运行耗时为：{}",usePoolVersion? "多线程模式":"单线程模式");
    }

    private void resumeTasks(List<Task> needResumeTasks) {
        log.info("需要恢复执行的任务数量：{}",needResumeTasks.size());
        needResumeTasks.forEach(t -> {
            Timer resumeTimer = new Timer();
            ResumeTaskShared.getResumeTaskTimerMap().put(String.valueOf(t.getId()),resumeTimer);
            ResumeTaskTimerTask resumeTaskTimerTask = new ResumeTaskTimerTask(t, callWarningService);
            resumeTimer.schedule(resumeTaskTimerTask,0, ObjectUtil.defaultIfNull(taskPeriod,3000));
            log.info("task:{} is resumed at {}", t.getId(), DateUtil.now());
        });
    }

    /**
     * 检查数据库中标志位运行的任务，实际上在磁盘上是不是已经结束了，结束了就更新数据库状态
     */
    private void checkTaskIsFinished(List<Task> needResumeTasks) {
        List<Task> checkFinishedTaskList = new ArrayList<>();
        needResumeTasks.forEach(t -> {
            boolean finishedFlag = FileUtil.exist(Paths.get(t.getOrderPath()).resolve(taskFinishedFileFlag).toFile());
            if (finishedFlag) {
                t.setTaskStatus(FINISHED);
                checkFinishedTaskList.add(t);
            }
        });
        if (CollectionUtils.isNotEmpty(checkFinishedTaskList)) {
            // 更新数据库中任务状态为结束
            taskService.updateBatchById(checkFinishedTaskList);
            needResumeTasks.removeAll(checkFinishedTaskList);
        }
    }
}
