package com.yhl.gj.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrLogVo {
    private String trackId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date logTime;
    private String errorInfo;
}
