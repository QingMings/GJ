package com.yhl.gj;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import net.sf.jsqlparser.expression.JsonAggregateOnNullType;
import org.junit.jupiter.api.Test;

public class JsonConvert {


    @Test
    void testConvertTOJson() {
        String str = "{\"msg\":\"综合态势显示信息 完成\",\"detail\":[{\"level\":1,\"utc\":\"2020-11-28 12:00:00.00\",\"target_id\":\"\",\"detail\":\"激光连续照射告警(照射持续时间: 5000 ms, 照射强度: 900 mW/cm^2, )\",\"type\":\"laser\"},{\"level\":3,\"utc\":\"2020-11-30 06:37:14.51\",\"target_id\":\"46611\",\"detail\":\"目标距离较近: ds=-1106.35m dt=-27110.71m dw=265.01m; 目标直线距离较近: dr=27134.57m; \",\"type\":\"orbit\"}]}";
        JSONObject jsonObject = JSON.parseObject(str);
        System.out.println(jsonObject);

    }
}
