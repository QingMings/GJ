package com.yhl.gj.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class CtnDto {
    @JSONField(name = "lv")
    private Double lv;
    @JSONField(name = "x")
    private Double x;
    @JSONField(name = "y")
    private Double y;
    @JSONField(name = "pd")
    private Double pd;
}
