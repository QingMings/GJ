package com.yhl.gj.vo;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.yhl.gj.model.TaskDetails;
import lombok.Data;

import java.util.Date;

@Data
public class TaskDetailsVO {
    // 任务详情ID
    private Long id;
    // 任务ID
    private Long taskId;
    // 任务名称 或主目标ID
    private String taskName;
    // 任务启动时间
    private Date createTime;
    // 告警等级
    private Integer warnLevel;
    // 威胁来源、类型（轨道接近，激光照射）
    private String menaceSource;
    // 任务磁盘路径
    private String orderPath;
    // 任务来源类型（自动、人工）
    private Integer taskType;
    @JSONField(serialize = false)
    private String  strategyStr;
    private String  targetDetails;
    // 规避策略
    private JSONObject strategy;

    private JSONObject runParams;

}
