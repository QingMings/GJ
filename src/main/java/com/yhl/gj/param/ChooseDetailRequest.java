package com.yhl.gj.param;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ChooseDetailRequest {

    /**
     * 任务ID
     */
    @NotNull(message = "taskId 必填")
    private Long  taskId;
    /**
     * 任务详情ID
     */
    @NotNull(message = "detailId 必填")
    private Long  detailId;
}
