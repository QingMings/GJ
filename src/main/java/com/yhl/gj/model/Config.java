package com.yhl.gj.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
    * 配置表
    */
@Data
@TableName(value = "HZGJ.GJ_CONFIG")
public class Config {
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    /**
     * 配置名称
     */
    @TableField(value = "CONFIG_NAME")
    private String configName;

    /**
     * 配置内容
     */
    @TableField(value = "CONFIG")
    private String config;

    /**
     * 任务id
     */
    @TableField(value = "TASK_ID")
    private Long taskId;

    /**
     * 任务详情id
     */
    @TableField(value = "DETAIL_ID")
    private Long detailId;

    /**
     * 默认配置标记
     */
    @TableField(value = "IS_DEFAULT")
    private Integer isDefault;

    public static final String COL_ID = "ID";

    public static final String COL_CONFIG_NAME = "CONFIG_NAME";

    public static final String COL_CONFIG = "CONFIG";

    public static final String COL_TASK_ID = "TASK_ID";

    public static final String COL_DETAIL_ID = "DETAIL_ID";

    public static final String COL_IS_DEFAULT = "IS_DEFAULT";
}