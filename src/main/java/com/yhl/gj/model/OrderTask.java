package com.yhl.gj.model;

import lombok.Data;

import java.util.Date;

/**
 * 订单任务
 */
@Data
public class OrderTask {

    private Long id;
    /**
     * 任务名称
     */
    private String taskName;
    /**
     * 任务给定磁盘路径
     */
    private String orderPath;
    /**
     * 任务创建时间(启动时间)
     */
    private Date createDate;
    /**
     * 任务更新时间
     */
    private Date updateDate;
    /**
     * 当前告警等级
     */
    private Integer  curWarningLevel;
    /**
     * 历史最高告警等级
     */
    private Integer  maxWarningLevel;
}
