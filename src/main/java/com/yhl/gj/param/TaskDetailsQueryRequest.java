package com.yhl.gj.param;

import com.yhl.gj.commons.base.BaseParam;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class TaskDetailsQueryRequest extends BaseParam {

    @NotNull(message = "任务Id必填")
    private Long taskId;

    /**
     * 告警等级 0、1、2、3
     */
    private List<Integer> warningLevel;
    /**
     * 任务来源 自动0、人工1
     */
    private List<Integer> taskType;
}
