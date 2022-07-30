package com.yhl.gj.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 数据驱动模式
 */
@Data
public class DataDriverParamRequest implements ParamRequest{

    /**
     * 磁盘路径
     */
    @NotNull(message = "orderPath 订单文件路径不能为空")
    @NotEmpty(message = "orderPath 订单文件路径不能为空")
    private String orderPath;

}
