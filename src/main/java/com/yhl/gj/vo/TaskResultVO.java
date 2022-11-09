package com.yhl.gj.vo;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.yhl.gj.dto.OrderDTO;
import com.yhl.gj.dto.StrategyDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@NoArgsConstructor
@Data
public class TaskResultVO {


    private Long id;
    private String orderId;
    private String taskStatus;
    private Long scanInterval;
    private Integer curWarnLevel;
    private String curWarnType;
    private Long pId;
    private Date createDate;
    private String workPath;
    private String outputPath;
    private String satelliteId;
    private OrderDTO order;
    private StrategyDTO strategy;
    private JSONObject chart;

}
