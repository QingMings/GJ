package com.yhl.gj.task;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.yhl.gj.commons.base.Response;
import com.yhl.gj.model.Task;
import com.yhl.gj.model.TaskDetails;
import com.yhl.gj.service.CallWarningService;
import com.yhl.gj.service.TaskDetailsService;
import com.yhl.gj.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 用于恢复程序异常退出后停止执行的任务
 */
@Slf4j
public class ResumeTaskTimerTask extends TimerTask {

    private final CallWarningService callWarningService;
    // 需要恢复的任务
    private Task task;

    private String taskFinishedFileFlag;
    private TaskService taskService;
    private TaskDetailsService detailsService;


    public ResumeTaskTimerTask(Task task,
                               TaskService taskService,
                               TaskDetailsService detailsService,
                               String taskFinishedFileFlag,
                               CallWarningService callWarningService) {
        this.task = task;
        this.taskService = taskService;
        this.detailsService = detailsService;
        this.taskFinishedFileFlag = taskFinishedFileFlag;
        this.callWarningService = callWarningService;
    }



    /**
     * 执行任务
     * 手动开启事务，出错回滚
     */
    @Override
    public void run() {
        log.info("task:{} is running at {}", task.getId(), DateUtil.now());
        Response response = callWarningService.executeTask(task,null);
        if (response.isSuccess()){
            log.info("task: {} is running success",task.getId());
        }else {
            log.info("task: {} is running fail,message:",response.getMessage());
        }

    }

    private TaskDetails findLastTaskDetails(){
      return detailsService.findLastTaskDetails(task.getId());
    }
    private String taskNameBuild(Long taskId, Long taskDetailsId) {
        return "任务" + taskId + "-告警" + taskDetailsId;
    }



    public void setPeriod(long period) {
        Date now = new Date();
        long nextExecutionTime = now.getTime() + period;
        // 设置下一次执行的时间
        setDeclaredField(TimerTask.class, this, "nextExecutionTime", nextExecutionTime);
        // 设置执行周期
        setDeclaredField(TimerTask.class, this, "period", period);
    }

    /**
     * 反射修改字段的值`
     */
    static boolean setDeclaredField(Class<?> clazz, Object obj, String name, Object value) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            field.set(obj, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
