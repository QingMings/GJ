package com.yhl.gj.param;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
@Data
public class WarnResultRequest {

    @NotNull(message = "warnLevels 必填！")
    private List<Integer> warnLevels;



}
