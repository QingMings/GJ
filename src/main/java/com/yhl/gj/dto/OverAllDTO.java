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
    @JSONField(name="dataUrl")
    private String detailUrl;
    @JSONField(name = "sateName")
    private String sateName;
    @JSONField(name = "sr_xyz")
    private Double[] sr_xyz;
    @JSONField(name = "pls")
    private PlsDto pls;
    @JSONField(name = "ctn")
    private  CtnDto ctn;
}
