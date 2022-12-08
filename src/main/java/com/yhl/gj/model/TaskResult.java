package com.yhl.gj.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 任务结果
 */
@Data
@TableName(value = "HZGJ.GJ_TASK_RESULT")
public class TaskResult {
    public static final String COL_CUR_WARN_LEVEL = "CUR_WARN_LEVEL";
    public static final String COL_CUR_WARN_TYPE = "CUR_WARN_TYPE";
    /**
     * ID
     */
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    /**
     * 任务名称或者任务ID
     */
    @TableField(value = "ORDER_ID")
    private String orderId;

    /**
     * 任务状态  working 工作中、finished 结束
     */
    @TableField(value = "TASK_STATUS")
    private String taskStatus;

    /**
     * 任务扫描间隔
     */
    @TableField(value = "SCAN_INTERVAL")
    private Long scanInterval;

    /**
     * 轨道接近当前告警等级 0、1、2、3
     */
    @TableField(value = "ORBIT_WARN_LEVEL")
    private Integer orbitWarnLevel;

    /**
     * 激光告警当前告警等级 0、1、2、3
     */
    @TableField(value = "LASER_WARN_LEVEL")
    private Integer laserWarnLevel;

    /**
     * 父级ID
     */
    @TableField(value = "P_ID")
    private Long pId;

    /**
     * 创建时间
     */
    @TableField(value = "CREATE_DATE")
    private Date createDate;

    /**
     * 订单输入路径
     */
    @TableField(value = "WORK_PATH")
    private String workPath;

    /**
     * 订单结果输出路径
     */
    @TableField(value = "OUTPUT_PATH")
    private String outputPath;

    /**
     * 卫星ID
     */
    @TableField(value = "SATELLITE_ID")
    private String satelliteId;

    /**
     * 更新时间
     */
    @TableField(value = "UPDATE_DATE")
    private Date updateDate;

    /**
     * 订单内容
     */
    @TableField(value = "\"ORDER\"")
    private String order;

    /**
     * 策略
     */
    @TableField(value = "STRATEGY")
    private String strategy;

    /**
     * 图标
     */
    @TableField(value = "CHART")
    private String chart;

    /**
     * 告警数据列表
     */
    @TableField(value = "ALARMS")
    private String alarms;

    /**
     * XML数据
     */
    @TableField(value = "PATH_GBCL_XML")
    private String pathGbclXml;

    public static final String COL_ID = "ID";

    public static final String COL_ORDER_ID = "ORDER_ID";

    public static final String COL_TASK_STATUS = "TASK_STATUS";

    public static final String COL_SCAN_INTERVAL = "SCAN_INTERVAL";

    public static final String COL_ORBIT_WARN_LEVEL = "ORBIT_WARN_LEVEL";

    public static final String COL_LASER_WARN_LEVEL = "LASER_WARN_LEVEL";

    public static final String COL_P_ID = "P_ID";

    public static final String COL_CREATE_DATE = "CREATE_DATE";

    public static final String COL_WORK_PATH = "WORK_PATH";

    public static final String COL_OUTPUT_PATH = "OUTPUT_PATH";

    public static final String COL_SATELLITE_ID = "SATELLITE_ID";

    public static final String COL_UPDATE_DATE = "UPDATE_DATE";

    public static final String COL_ORDER = "ORDER";

    public static final String COL_STRATEGY = "STRATEGY";

    public static final String COL_CHART = "CHART";

    public static final String COL_ALARMS = "ALARMS";

    public static final String COL_PATH_GBCL_XML = "PATH_GBCL_XML";
}