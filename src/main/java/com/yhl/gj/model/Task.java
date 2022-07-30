package com.yhl.gj.model;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import com.yhl.gj.commons.constant.Constants;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务表
 */
@Data
@TableName(value = "HZGJ.GJ_TASK")
@NoArgsConstructor
public class Task {
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    public Task(String taskName, String orderPath) {
        this.taskName = taskName;
        this.orderPath = orderPath;
        this.createDate = DateUtil.date();
        this.taskStatus = Constants.RUNNING;
    }

    public Task(String orderPath) {
        this.orderPath = orderPath;
        this.createDate = DateUtil.date();
        this.taskStatus = Constants.RUNNING;
    }

    /**
     * 任务名称或者主卫星ID
     */
    @TableField(value = "TASK_NAME")
    private String taskName;

    /**
     * 订单文件磁盘路径
     */
    @TableField(value = "ORDER_PATH")
    private String orderPath;

    /**
     * 创建时间、启动时间
     */
    @TableField(value = "CREATE_DATE")
    private Date createDate;

    /**
     * 更新时间
     */
    @TableField(value = "UPDATE_DATE")
    private Date updateDate;

    /**
     * 当前告警等级
     */
    @TableField(value = "CUR_WARN_LEVEL")
    private Integer curWarnLevel;

    /**
     * 历史最高危险等级
     */
    @TableField(value = "MAX_WARN_LEVEL")
    private Integer maxWarnLevel;

    /**
     * 选中的规避策略
     */
    @TableField(value = "CHECKED_DETAIL_ID")
    private Long checkedDetailId;

    /**
     * 任务状态（执行中、已结束）
     */
    @TableField(value = "TASK_STATUS")
    private Integer taskStatus;

    public static final String COL_ID = "ID";

    public static final String COL_TASK_NAME = "TASK_NAME";

    public static final String COL_ORDER_PATH = "ORDER_PATH";

    public static final String COL_CREATE_DATE = "CREATE_DATE";

    public static final String COL_UPDATE_DATE = "UPDATE_DATE";

    public static final String COL_CUR_WARN_LEVEL = "CUR_WARN_LEVEL";

    public static final String COL_MAX_WARN_LEVEL = "MAX_WARN_LEVEL";

    public static final String COL_CHECKED_DETAIL_ID = "CHECKED_DETAIL_ID";

    public static final String COL_TASK_STATUS = "TASK_STATUS";
}