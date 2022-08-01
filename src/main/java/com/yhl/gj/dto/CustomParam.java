package com.yhl.gj.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 自定义参数执行任务
 */
@Data
public class CustomParam {
    @NotNull
    // -判断门限/依据配置文件
    private Gates gates;
    // 预警时间区间
    @NotNull
    private TimeSpan timeSpan;
    // 公共参数：跳秒
    @NotNull
    private String param_LEAP;
    // 公共参数：极移
    @NotNull
    private String param_EOP;
    // 公共参数：大气环境
    @NotNull
    private String param_SWD;
    // 预报误差参数
    private String param_ERR;



    //预警时间区间
    @Data
    public class TimeSpan {
        // 时间区间
        @NotNull
        private Double days;
    }

    // -判断门限/依据配置文件
    @Data
    public class Gates {
        // 直线距离告警门限[米]
        @NotNull
        private BigDecimal gate_dr ;
        // 预计碰撞时间门限[天]. 若预计碰撞时间>gate_etca则认为碰撞时间不紧迫
        @NotNull
        private BigDecimal gate_etca;
        // dsw三轴距离门限[米].
        @NotNull
        private BigDecimal[] gate_dSTW;
        // 碰撞概率判断门限[无量纲].
        @NotNull
        private BigDecimal gate_pc;
    }
}
