package com.yhl.gj.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@TableName(value = "HZGJ.GJ_WARN_RESULT")
public class WarnResult {
    /**
     * ID
     */
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    @TableField(value = "TASK_ID")
    private Long taskId;

    /**
     * 卫星ID
     */
    @TableField(value = "SATELLITE_ID")
    private String satelliteId;

    /**
     * 任务名称
     */
    @TableField(value = "ORDER_ID")
    private String orderId;

    /**
     * 告警类型 laser
     */
    @TableField(value = "WARN_TYPE")
    private String warnType;

    /**
     * 告警等级
     */
    @TableField(value = "WARN_LEVEL")
    private Integer warnLevel;

    /**
     * 告警时间
     */
    @TableField(value = "WARN_TIME_UTC")
    private Date warnTimeUtc;

    /**
     * 告警信息
     */
    @TableField(value = "WARN_INFO")
    private String warnInfo;

    /**
     * 威胁来源
     */
    @TableField(value = "TARGET_ID")
    private String targetId;

    /**
     * 告警状态   0 未归档  1 已归档
     */
    @TableField(value = "WARN_STATUS")
    private Integer warnStatus;

    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME")
    private Date createTime;

    public static final String COL_ID = "ID";

    public static final String COL_TASK_ID = "TASK_ID";

    public static final String COL_SATELLITE_ID = "SATELLITE_ID";

    public static final String COL_ORDER_ID = "ORDER_ID";

    public static final String COL_WARN_TYPE = "WARN_TYPE";

    public static final String COL_WARN_LEVEL = "WARN_LEVEL";

    public static final String COL_WARN_TIME_UTC = "WARN_TIME_UTC";

    public static final String COL_WARN_INFO = "WARN_INFO";

    public static final String COL_TARGET_ID = "TARGET_ID";

    public static final String COL_WARN_STATUS = "WARN_STATUS";

    public static final String COL_CREATE_TIME = "CREATE_TIME";
}