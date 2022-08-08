package com.yhl.gj.vo;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogVo {

    private String trackId;
    private String orderType;
    private String logDetail;
    private String logType;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date logTime;
    private String code;
}
