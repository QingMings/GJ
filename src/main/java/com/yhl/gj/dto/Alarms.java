package com.yhl.gj.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class Alarms {
    @JSONField(name = "laser")
    private List<LaserDTO> laser;
    @JSONField(name = "orbit")
    private List<OrbitDTO> orbit;

    @NoArgsConstructor
    @Data
    public static class LaserDTO {
        @JSONField(name = "lv")
        private Integer lv;
        @JSONField(name = "utc")
        private String utc;
        @JSONField(name = "pls")
        private PlsDTO pls;
        @JSONField(name = "ctn")
        private CtnDTO ctn;

        @NoArgsConstructor
        @Data
        public static class PlsDTO {
            @JSONField(name = "lv")
            private Integer lv;
            @JSONField(name = "x")
            private Double x;
            @JSONField(name = "y")
            private Double y;
            @JSONField(name = "hz")
            private Integer hz;
        }

        @NoArgsConstructor
        @Data
        public static class CtnDTO {
            @JSONField(name = "lv")
            private Integer lv;
            @JSONField(name = "x")
            private Double x;
            @JSONField(name = "y")
            private Double y;
            @JSONField(name = "pd")
            private Integer pd;
        }
    }

    @NoArgsConstructor
    @Data
    public static class OrbitDTO {
        @JSONField(name = "utc")
        private String utc;
        @JSONField(name = "lv")
        private Integer lv;
        @JSONField(name = "norad")
        private Integer norad;
        @JSONField(name = "dr")
        private Double dr;
        @JSONField(name = "ds")
        private Double ds;
        @JSONField(name = "dt")
        private Double dt;
        @JSONField(name = "dw")
        private Double dw;
        @JSONField(name = "pc")
        private Double pc;
    }
}
