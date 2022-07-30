package com.yhl.gj.vo;

import lombok.Data;

import java.util.Date;

/**
 * 任务vo
 */
@Data
public class TaskVO {

    private Long id;
    private String taskName; // 主卫星名字
    private String orderPath;
    private Date createDate;
    private Date updateDate;
    private Integer curWarnLevel;
    private Integer maxWarnLevel;
    private Long lastDetailId; // 最新任务详情ID(根据任务详情id，找最新的规避策略)
    private Long checkedDetailId;// 选中的任务详情ID(根据任务详情id，查找回显规避策略)
    private Integer taskStatus; // 任务状态（执行中1、已结束0）
}
