package com.yhl.gj.task;

import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

public class OrderTaskShared {
    // 存放任务Timer，key为taskId,value 是 OrderTaskShared
    private static final ConcurrentHashMap<String, Timer> orderTaskTimerMap = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, Timer> getOrderTaskTimerMap() {
        return orderTaskTimerMap;
    }
}
