package com.yhl.gj.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 日志记录表
 */
@Data
@TableName(value = "HZGJ.GJ_LOG")
public class Log {
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    /**
     * OrderID 订单号
     */
    @TableField(value = "TRACK_ID")
    private String trackId;

    /**
     * 订单类型: user_face 、data_driver
     */
    @TableField(value = "ORDER_TYPE")
    private String orderType;

    public static final String COL_LOG_TIME = "LOG_TIME";
    public static final String COL_CODE = "CODE";

    /**
     * 日志详情
     */
    @TableField(value = "LOG_DETAIL")
    private String logDetail;
    /**
     * 日志类型 Progress、Result
     */
    @TableField(value = "LOG_TYPE")
    private String logType;

    public static final String COL_ID = "ID";

    public static final String COL_TRACK_ID = "TRACK_ID";

    public static final String COL_ORDER_TYPE = "ORDER_TYPE";

    public static final String COL_LOG_TYPE = "LOG_TYPE";
    /**
     * 日志产生时间
     */
    @TableField(value = "LOG_TIME")
    private Date logTime;

    public static final String COL_LOG_DETAIL = "LOG_DETAIL";
    /**
     * 日志编码
     */
    @TableField(value = "CODE")
    private String code;
}