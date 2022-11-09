package com.yhl.gj.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class OrderDTO {
    @JSONField(name = "output")
    private String output;
    @JSONField(name = "input")
    private InputDTO input;
    @JSONField(name = "task")
    private TaskDTO task;
    @JSONField(name = "params")
    private ParamsDTO params;

    @NoArgsConstructor
    @Data
    public static class InputDTO {
        @JSONField(name = "path_input_root")
        private String pathInputRoot;
    }

    @NoArgsConstructor
    @Data
    public static class TaskDTO {
        @JSONField(name = "productLevel")
        private String productLevel;
        @JSONField(name = "satelliteID")
        private String satelliteID;
        @JSONField(name = "priority")
        private String priority;
        @JSONField(name = "order_id")
        private String orderId;
        @JSONField(name = "scan_interval")
        private Integer scanInterval;
    }

    @NoArgsConstructor
    @Data
    public static class ParamsDTO {
        @JSONField(name = "time_span_days")
        private Integer timeSpanDays;
        @JSONField(name = "time_start_utc")
        private String timeStartUtc;
        @JSONField(name = "path_eop")
        private String pathEop;
        @JSONField(name = "time_end_utc")
        private String timeEndUtc;
        @JSONField(name = "path_leap")
        private String pathLeap;
        @JSONField(name = "path_swd")
        private String pathSwd;
        @JSONField(name = "thresholds")
        private ThresholdsDTO thresholds;

        @NoArgsConstructor
        @Data
        public static class ThresholdsDTO {
            @JSONField(name = "gate_etca")
            private Double gateEtca;
            @JSONField(name = "gate_dSTW")
            private List<Double> gateDstw;
            @JSONField(name = "gate_dr")
            private Double gateDr;
            @JSONField(name = "gate_pc")
            private Double gatePc;
        }
    }
}
