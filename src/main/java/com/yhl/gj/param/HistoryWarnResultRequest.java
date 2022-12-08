package com.yhl.gj.param;

import com.yhl.gj.commons.base.BaseParam;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
public class HistoryWarnResultRequest extends BaseParam {

    private List<Integer> laserWarnLevels;
    private List<Integer> orbitWarnLevels;


    private String taskName;
    private List<String> satelliteNames;

    @NotNull(message = "请输入开始时间")
    private Date startTime;
    /**
     * 结束时间
     */
    @NotNull(message = "请输入结束时间")
    private Date endTime;
}
