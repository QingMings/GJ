package com.yhl.gj.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
    * 日志记录表
    */
@Data
@TableName(value = "SYSDBA.GJ_LOG")
public class Log {
    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

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

    /**
     * 日志类型 Progress、Result
     */
    @TableField(value = "LOG_TYPE")
    private String logType;

    /**
     * 日志产生时间
     */
    @TableField(value = "LOG_TIME")
    private Date logTime;

    /**
     * 日志详情
     */
    @TableField(value = "LOG_DETAIL")
    private String logDetail;

    public static final String COL_ID = "ID";

    public static final String COL_TRACK_ID = "TRACK_ID";

    public static final String COL_ORDER_TYPE = "ORDER_TYPE";

    public static final String COL_LOG_TYPE = "LOG_TYPE";

    public static final String COL_LOG_TIME = "LOG_TIME";

    public static final String COL_LOG_DETAIL = "LOG_DETAIL";
}