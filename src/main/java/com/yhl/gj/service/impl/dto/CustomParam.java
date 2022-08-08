package com.yhl.gj.service.impl.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 自定义参数执行任务
 */
@Data
public class CustomParam {
    @NotNull
    // -判断门限/依据配置文件
    @Valid
    private Thresholds thresholds;
    // 预警时间区间
    @NotNull
    private Double time_span_days;
    // 公共参数：跳秒
    @NotNull(message = "path_leap 路径为空")
    @NotEmpty(message = "path_leap 路径为空")
    private String path_leap;
    // 公共参数：极移
    @NotNull(message = "path_eop 路径为空")
    @NotEmpty(message = "path_eop 路径为空")
    private String path_eop;
    // 公共参数：大气环境
    @NotNull(message = "path_swd 路径为空")
    @NotEmpty(message = "path_swd 路径为空")
    private String path_swd;
    // 预报误差参数
    @NotNull(message = "path_error 路径为空")
    @NotEmpty(message = "path_error 路径为空")
    private String path_error;



    // -判断门限/依据配置文件
    @Data
    public class Thresholds {
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
        // 连续激光照射功率
        @NotNull
        private BigDecimal thr_las_ctn_dur;
        @NotNull
        private BigDecimal thr_las_ctn_level_0;
        @NotNull
        private BigDecimal thr_las_ctn_level_1;
    }
}
