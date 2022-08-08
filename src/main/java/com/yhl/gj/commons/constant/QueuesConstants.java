package com.yhl.gj.commons.constant;

/**
 * 队列
 */
public interface QueuesConstants {


    /**
     * 系统log信息处理
     */
    String SYS_LOG_ADD_EXCHANGE = "sys_log_add_exchange";
    String SYS_LOG_ADD_QUEUE = "sys_log_add_queue";
    String SYS_LOG_ADD_ROUTE_KEY = "sys_log_add_route_key";

    /**
     * 态势信息上报
     */
    String WARN_REPORT_EXCHANGE= "warn_report_exchange";
    String WARN_REPORT_QUEUE= "warn_report_queue";
    String WARN_REPORT_ROUTE_KEY= "warn_report_route_key";

    /**
     * 触发执行一次任务
     */
    String TASK_TRIGGER_EXCHANGE = "task_trigger_exchange";
    String TASK_TRIGGER_QUEUE = "task_trigger_queue";
    String TASK_TRIGGER_ROUTE_KEY = "task_trigger_route_key";
}
