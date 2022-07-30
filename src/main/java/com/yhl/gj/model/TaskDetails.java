package com.yhl.gj.model;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 任务详情表
 */
@Data
@TableName(value = "HZGJ.GJ_TASK_DETAILS")
public class TaskDetails {
    public TaskDetails(Long taskId, String orderPath, String taksName) {
        this.taskId = taskId;
        this.orderPath = orderPath;
        this.createTime = DateUtil.date();
        this.taskName = taksName;
    }
    /**
     * 任务详情表ID
     */
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    /**
     * 任务表ID
     */
    @TableField(value = "TASK_ID")
    private Long taskId;

    /**
     * 任务路径
     */
    @TableField(value = "ORDER_PATH")
    private String orderPath;

    /**
     * 任务启动时间
     */
    @TableField(value = "CREATE_TIME")
    private Date createTime;

    /**
     * 任务名称或者主目标ID
     */
    @TableField(value = "TASK_NAME")
    private String taskName;

    /**
     * 告警等级
     */
    @TableField(value = "WARN_LEVEL")
    private Integer warnLevel;

    /**
     * 威胁来源
     */
    @TableField(value = "MENACE_SOURCE")
    private String menaceSource;

    /**
     * 任务来源、类型（自动，手动）
     */
    @TableField(value = "TASK_TYPE")
    private Integer taskType;

    /**
     * 运行参数
     */
    @TableField(value = "RUN_PARAMS")
    private String runParams;

    /**
     * 目标详情（before和arter 两个文件路径）
     */
    @TableField(value = "TARGET_DETAILS")
    private String targetDetails;

    /**
     * 规避策略
     */
    @TableField(value = "STRATEGY")
    private String strategy;

    public static final String COL_ID = "ID";

    public static final String COL_TASK_ID = "TASK_ID";

    public static final String COL_ORDER_PATH = "ORDER_PATH";

    public static final String COL_CREATE_TIME = "CREATE_TIME";

    public static final String COL_TASK_NAME = "TASK_NAME";

    public static final String COL_WARN_LEVEL = "WARN_LEVEL";

    public static final String COL_MENACE_SOURCE = "MENACE_SOURCE";

    public static final String COL_TASK_TYPE = "TASK_TYPE";

    public static final String COL_RUN_PARAMS = "RUN_PARAMS";

    public static final String COL_TARGET_DETAILS = "TARGET_DETAILS";

    public static final String COL_STRATEGY = "STRATEGY";
}