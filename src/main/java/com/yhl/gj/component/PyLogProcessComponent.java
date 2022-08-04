package com.yhl.gj.component;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yhl.gj.commons.constant.PyLogType;
import com.yhl.gj.model.Log;
import com.yhl.gj.service.CallWarningService;
import com.yhl.gj.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.yhl.gj.commons.constant.Constants.*;

@Slf4j
@Component
public class PyLogProcessComponent {

    @Resource
    private Pattern pyLogRegexPattern;
    @Resource
    private LogService logService;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private CallWarningService callWarningService;


    private void saveLogToDB() {

    }

    /**
     * 设置日志时间
     *
     * @param logTime
     * @return
     */
    private Date setUTCTime(String logTime) {
        LocalDateTime localDateTime = DateUtil.parseLocalDateTime(logTime, "yyyy-MM-dd HH:mm:ss.SSSSSS");
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
        Instant instant = zonedDateTime.toInstant();
        ;
        return Date.from(instant);
    }

    /**
     * python 日志 输出
     */
    public void pythonLogHandle(List<String> pylogs, String logTrackId, String model) {
        List<Log> logs = new ArrayList<>();
        for (String pylog : pylogs) {
            Matcher m = pyLogRegexPattern.matcher(pylog);
            if (!m.find()) {
                log.error(pylog);
                continue;
            }
            Log logInfo = createPyLog(m, model, logTrackId);
            logs.add(logInfo);
        }
        // 保存日志到数据库
        logService.saveBatch(logs);
        // 次目标告警结果收集
        JSONArray target_Orbit_GJ_events = new JSONArray();
        // 搜集结果
        JSONObject resultCollect = new JSONObject();
        resultCollect.put(TargetOrbit, target_Orbit_GJ_events);


        logs.stream().filter(l -> PyLogType.RESULT.equals(StrUtil.trim(l.getLogType()))).forEach(l -> {
            switch (l.getCode()) {
                case "100":   // 次目标轨道接近事件 生成告警信息 完成
                    JSONObject targetOrbit_GJ = JSON.parseObject(l.getLogDetail());
                    target_Orbit_GJ_events.add(targetOrbit_GJ.getJSONObject(Detail));
                    break;
                case "110":  // 计算规避策略 完成
                    JSONObject strategy = JSON.parseObject(l.getLogDetail());
                    JSONObject strategyDetail = strategy.getJSONObject(Detail);
                    resultCollect.put(STRATEGY, strategyDetail);
                    break;
                case "120":  // 激光告警 完成
                    JSONObject targetLaser_GJ = JSON.parseObject(l.getLogDetail());
                    JSONArray laser_detail = targetLaser_GJ.getJSONArray(Detail);
                    resultCollect.put(TargetLaser, laser_detail);
                    break;
                case "130":  // 系统观测精度评估 完成
                    JSONObject pinGu = JSON.parseObject(l.getLogDetail());
                    JSONObject pinGu_detail = pinGu.getJSONObject(Detail);
                    String filePath = pinGu_detail.getString(Path);
                    if (FileUtil.exist(filePath)) {
                        String fileContent = FileUtil.readUtf8String(filePath);
                        JSONArray jsonFile = JSON.parseArray(fileContent);
                        resultCollect.put(PinGu, jsonFile);
                    }
                    break;
                case "140":  // 写入相对位置关系描点数据文件 完成
                    JSONObject positionFile = JSON.parseObject(l.getLogDetail());
                    JSONObject detailObject = positionFile.getJSONObject(Detail);
                    if (!detailObject.isEmpty()){
                        detailObject.forEach((k,v) -> {
                            JSONObject positionObject = detailObject.getJSONObject(k);
                            if (positionObject.containsKey(before)){
                                positionHandle(positionObject,before);
                            }
                            if (positionObject.containsKey(after)){
                                positionHandle(positionObject,after);
                            }
                        });
                    }
                    resultCollect.put("position_relation",detailObject);
                    break;
                case "150":
                    JSONObject max_GJ = JSON.parseObject(l.getLogDetail());
                    JSONObject maxDetail = max_GJ.getJSONObject(Detail);
                    resultCollect.put(MAX_GJ, maxDetail);
                    break;
            }
        });

        callWarningService.updateTaskStrategy(resultCollect, logTrackId);
    }

    private void positionHandle(JSONObject positionObject,String key){
        String content = FileUtil.readUtf8String(positionObject.getString(key));
        JSONObject contentObject = JSON.parseObject(content);
         JSONArray pointsJsonArray = contentObject.getJSONArray("points");
        List<BigDecimal[]> chart1dataList = new ArrayList<>();
        List<BigDecimal>  chart2dataList = new ArrayList<>();
        List<String> chart2utcList = new ArrayList<>();
         pointsJsonArray.forEach(o -> {
             JSONObject point = (JSONObject) o;
             BigDecimal[] pointArray = new BigDecimal[]{point.getBigDecimal("dt"),point.getBigDecimal("ds")};
             chart1dataList.add(pointArray);
             chart2dataList.add(point.getBigDecimal("dist"));
             chart2utcList.add(point.getString("utc"));
         });
         JSONObject output = new JSONObject();
         output.put("satid_p",contentObject.getIntValue("satid_p"));
         output.put("satid_s",contentObject.getIntValue("satid_s"));
         output.put("chart1",chart1dataList.toArray());
         JSONObject chart2 = new JSONObject();
         chart2.put("x",chart2utcList.toArray());
         chart2.put("y",chart2dataList.toArray());
         output.put("chart2",chart2);

        positionObject.put(key,output);
    }

    private Log createPyLog(Matcher m, String model, String logTrackId) {
        String logType = m.group(1);
        String logTime = m.group(2);
        String logCode = m.group(3);
        String logDetails = m.group(4);
        Log logInfo = new Log();
        logInfo.setOrderType(model);
        logInfo.setCode(logCode);
        logInfo.setLogDetail(logDetails);
        logInfo.setLogTime(setUTCTime(logTime));
        logInfo.setLogType(logType);
        logInfo.setTrackId(logTrackId);
        return logInfo;
    }
}
