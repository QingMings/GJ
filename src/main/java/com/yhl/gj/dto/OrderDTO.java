package com.yhl.gj.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@NoArgsConstructor
@Data
public class OrderDTO {
    @NotNull(message = "参数 output 必填")
    @JSONField(name = "output")
    private String output;

    @Valid
    @JSONField(name = "input")
    private InputDTO input;

    @Valid
    @JSONField(name = "task")
    private TaskDTO task;

    @Valid
    @JSONField(name = "params")
    private ParamsDTO params;

    @NoArgsConstructor
    @Data
    public static class InputDTO {
        @NotNull(message = "参数 pathInputRoot 必填")
        @JSONField(name = "path_input_root")
        private String pathInputRoot;
    }

    @NoArgsConstructor
    @Data
    public static class TaskDTO {
        @NotNull(message = "参数 productLevel 必填")
        @JSONField(name = "productLevel")
        private String productLevel;

        @NotNull(message = "参数 satelliteID 必填")
        @JSONField(name = "satelliteID")
        private String satelliteID;

        @NotNull(message = "参数 priority 必填")
        @JSONField(name = "priority")
        private String priority;

        @NotNull(message = "参数 orderId 必填")
        @JSONField(name = "order_id")
        private String orderId;

        @NotNull(message = "参数 scanInterval 必填")
        @JSONField(name = "scan_interval")
        private Integer scanInterval;
    }

    @NoArgsConstructor
    @Data
    public static class ParamsDTO {
        @NotNull(message = "参数 timeSpanDays 必填")
        @JSONField(name = "time_span_days")
        private Integer timeSpanDays;

        @NotNull(message = "参数 timeStartUtc 必填")
        @JSONField(name = "time_start_utc")
        private String timeStartUtc;

        @NotNull(message = "参数 pathEop 必填")
        @JSONField(name = "path_eop")
        private String pathEop;

        @NotNull(message = "参数 timeEndUtc 必填")
        @JSONField(name = "time_end_utc")
        private String timeEndUtc;

        @NotNull(message = "参数 pathLeap 必填")
        @JSONField(name = "path_leap")
        private String pathLeap;

        @NotNull(message = "参数 pathSwd 必填")
        @JSONField(name = "path_swd")
        private String pathSwd;

        @Valid
        @JSONField(name = "thresholds")
        private ThresholdsDTO thresholds;

        @NoArgsConstructor
        @Data
        public static class ThresholdsDTO {

            @NotNull(message = "参数 gateEtca 必填")
            @JSONField(name = "gate_etca")
            private Double gateEtca;

            @NotNull(message = "参数 gateDstw 必填")
            @JSONField(name = "gate_dSTW")
            private List<Double> gateDstw;

            @NotNull(message = "参数 gateDr 必填")
            @JSONField(name = "gate_dr")
            private Double gateDr;

            @NotNull(message = "参数 gatePc 必填")
            @JSONField(name = "gate_pc")
            private Double gatePc;
        }
    }
}
