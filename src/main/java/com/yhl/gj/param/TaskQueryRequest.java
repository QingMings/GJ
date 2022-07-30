package com.yhl.gj.param;

import com.yhl.gj.commons.base.BaseParam;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
public class TaskQueryRequest extends BaseParam {

    /**
     * 开始时间
     */
    @NotNull(message = "请输入开始时间")
    private Date startTime;
    /**
     * 结束时间
     */
    @NotNull(message = "请输入结束时间")
    private Date endTime;
    /**
     * 当前告警等级 告警等级 0、1、2、3
     */
    private List<Integer> currentWarnLevel;
    /**
     * 最大告警等级 0、1、2、3
     */
    private List<Integer> maxWarnLevel;
    private List<String> taskNames;



}
