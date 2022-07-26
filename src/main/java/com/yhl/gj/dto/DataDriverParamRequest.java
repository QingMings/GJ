package com.yhl.gj.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class DataDriverParamRequest implements ParamRequest{

    @NotNull(message = "orderXmlPath 订单文件路径不能为空")
    @NotEmpty(message = "orderXmlPath 订单文件路径不能为空")
    private String orderXmlPath;

}
