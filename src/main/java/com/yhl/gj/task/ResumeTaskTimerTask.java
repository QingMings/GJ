package com.yhl.gj.task;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import com.yhl.gj.model.Task;
import com.yhl.gj.service.TaskDetailsService;
import com.yhl.gj.service.TaskService;
import lombok.extern.slf4j.Slf4j;

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

    // 需要恢复的任务
    private Task task;

    private String taskFinishedFileFlag;
    private TaskService taskService;
    private TaskDetailsService detailsService;


    public ResumeTaskTimerTask(Task task,
                               TaskService taskService,
                               TaskDetailsService detailsService,
                               String taskFinishedFileFlag) {
        this.task = task;
        this.taskService = taskService;
        this.detailsService = detailsService;
        this.taskFinishedFileFlag = taskFinishedFileFlag;
    }

    public ResumeTaskTimerTask() {
    }

    @Override
    public void run() {

        log.info("task:{} is running at {}", task.getId(), DateUtil.now());



        checkTaskFinished();
    }

    private String taskNameBuild(Long taskId, Long taskDetailsId) {
        return "任务" + taskId + "-告警" + taskDetailsId;
    }

    /**
     * 检查是否
     */
    private void checkTaskFinished(){
        boolean checkTaskFinishedFlag = FileUtil.exist(Paths.get(task.getOrderPath()).resolve(taskFinishedFileFlag).toFile());
        if (checkTaskFinishedFlag){
            // 1. 标记数据库中task 状态 为FINISHED
            String taskId = String.valueOf(task.getId());
            Timer taskTimer = ResumeTaskShared.getResumeTaskTimerMap().get(taskId);
            if(ObjectUtil.isNotNull(taskTimer)){
                // 2.停止Timer
                taskTimer.cancel();
                // 3. 从map中移除taskId
                ResumeTaskShared.getResumeTaskTimerMap().remove(taskId);
                log.info("task:{} is finished",taskId);
            }
        }
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
