package com.yhl.gj.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class Moves {
    @JSONField(name = "moves")
    private List<MovesDTO> moves;
    @JSONField(name = "moves_count")
    private Integer movesCount;
    @JSONField(name = "delta_v_all")
    private Double deltaVAll;

    @NoArgsConstructor
    @Data
    public static class MovesDTO {
        @JSONField(name = "vecdv_t")
        private List<Double> vecdvT;
        @JSONField(name = "vecdv_xyz")
        private List<Double> vecdvXyz;
        @JSONField(name = "utc")
        private String utc;
        @JSONField(name = "vecv_minus")
        private List<Double> vecvMinus;
        @JSONField(name = "vecr")
        private List<Double> vecr;
        @JSONField(name = "type")
        private Integer type;
    }
}
