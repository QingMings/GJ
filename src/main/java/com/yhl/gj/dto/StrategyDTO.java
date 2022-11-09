package com.yhl.gj.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class StrategyDTO {
    @JSONField(name = "moves")
    private Moves moves;
    @JSONField(name = "overall")
    private List<OverAllDTO> overall;
}
