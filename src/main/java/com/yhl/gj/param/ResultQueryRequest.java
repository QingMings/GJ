package com.yhl.gj.param;

import com.yhl.gj.commons.base.BaseParam;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
public class ResultQueryRequest extends BaseParam {

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
     * 轨道接近告警等级 告警等级 0、1、2、3
     */
    private List<Integer> orbitWarnLevel;

    /**
     * 激光照射告警等级 告警等级 0、1、2、3
     */
    private List<Integer> laserWarnLevel;
    /**
     * 卫星名称
     */
    private List<String> satellites;

    /**
     * 任务状态
     */
    private String taskStatus;
    /**
     * 任务类型
     */
    private String taskType;

    private String taskName;
}
