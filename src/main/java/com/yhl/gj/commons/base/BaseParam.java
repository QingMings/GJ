package com.yhl.gj.commons.base;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class BaseParam {

    /**
     * 页码
     */
    @NotNull(message = "分页参数不能为空")
    private Integer currentPage;
    /**
     * 每页数据条数
     */
    @NotNull(message = "分页参数不能为空")
    private Integer pageSize;
}
