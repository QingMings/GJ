package com.yhl.gj.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class OverAllDTO {
    @JSONField(name = "level")
    private Integer level;
    @JSONField(name = "utc")
    private String utc;
    @JSONField(name = "target_id")
    private String targetId;
    @JSONField(name = "detail")
    private String detail;
    @JSONField(name = "type")
    private String type;
}
