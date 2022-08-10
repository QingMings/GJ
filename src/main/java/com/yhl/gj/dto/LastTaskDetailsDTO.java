package com.yhl.gj.dto;

import lombok.Data;

import java.util.Date;

@Data
public class LastTaskDetailsDTO {
    // 任务ID
    private Long taskId;
    // 任务详情ID
    private Long id;
    // 任务详情创建时间
    private Date createTime;
}
