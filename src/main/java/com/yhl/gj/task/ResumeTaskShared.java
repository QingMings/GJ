package com.yhl.gj.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

public class ResumeTaskShared {
    // 存放需要恢复的任务Timer，key为taskId,value 是 ResumeTaskTimeTask
    private static ConcurrentHashMap<String, Timer> resumeTaskTimerMap = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, Timer> getResumeTaskTimerMap() {
        return resumeTaskTimerMap;
    }
}
