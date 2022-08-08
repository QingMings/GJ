package com.yhl.gj.component;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yhl.gj.commons.constant.PyLogType;
import com.yhl.gj.commons.constant.QueuesConstants;
import com.yhl.gj.model.Log;
import com.yhl.gj.service.CallWarningService;
import com.yhl.gj.service.LogService;
import com.yhl.gj.vo.ErrLogVo;
import com.yhl.gj.vo.LogVo;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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
    private Pattern warnReportRegexPattern;

    @Resource
    private Pattern maxWarnLevelRegexPattern;
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
                errorSendToMQ(pylog,logTrackId);
                log.error(pylog);
                continue;
            }
//            log.info(pylog);
            Log logInfo = createPyLog(m, model, logTrackId);
            logs.add(logInfo);
        }
        // 保存日志到数据库
        logService.saveBatch(logs);
        sendSysLogToMQ(logs);
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
                    if (!detailObject.isEmpty()) {
                        detailObject.forEach((k, v) -> {
                            JSONObject positionObject = detailObject.getJSONObject(k);
                            if (positionObject.containsKey(before)) {
                                positionHandle(positionObject, before);
                            }
                            if (positionObject.containsKey(after)) {
                                positionHandle(positionObject, after);
                            }
                        });
                    }
                    resultCollect.put("position_relation", detailObject);
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

    public void errorSendToMQ(String pylog, String logTrackId) {
        ErrLogVo errLogVo = new ErrLogVo();
        errLogVo.setLogTime(DateUtil.date());
        errLogVo.setTrackId(logTrackId);
        errLogVo.setErrorInfo(pylog);
        rabbitTemplate.convertAndSend(
                QueuesConstants.SYS_LOG_ADD_EXCHANGE,
                QueuesConstants.SYS_LOG_ADD_ROUTE_KEY,
                JSON.toJSONString(errLogVo));
    }

    /**
     * 将code=151发送到mq
     */
    public void code151Handle(String pylog, String logTrackId, String model) {
        Matcher matcher = warnReportRegexPattern.matcher(pylog);
        if (!matcher.find()) {
            return;
        }
        log.info("warn report 150  to mq :{}", logTrackId);
        Log logInfo = createPyLog151(matcher, model, logTrackId);
        LogVo logVo = convertToLogVO(logInfo);
        rabbitTemplate.convertAndSend(QueuesConstants.WARN_REPORT_EXCHANGE, QueuesConstants.WARN_REPORT_ROUTE_KEY, logVo);
    }

    /**
     * 150输出时候，先去更新数据库告警等级字段，
     */
    public void code150Handle(String pylog, String logTrackId, String model) {
        Matcher matcher = maxWarnLevelRegexPattern.matcher(pylog);
        if (!matcher.find()) {
            return;
        }

        Log logInfo = createPyLog151(matcher, model, logTrackId);
        JSONObject max_GJ = JSON.parseObject(logInfo.getLogDetail());
        JSONObject maxDetail = max_GJ.getJSONObject(Detail);
        try {

        callWarningService.updateTaskWarnLevel(maxDetail,logTrackId);
        }catch (Exception e){
            log.error("handle code 150 error: {}",e.getMessage());
        }

    }
    /**
     * 将运行日志发送到mq
     */
    private void sendSysLogToMQ(List<Log> logs) {
        logs.forEach(l -> {

            LogVo logVo = convertToLogVO(l);
            rabbitTemplate.convertAndSend(
                    QueuesConstants.SYS_LOG_ADD_EXCHANGE,
                    QueuesConstants.SYS_LOG_ADD_ROUTE_KEY,
                    JSON.toJSONString(logVo));
        });
    }

    private LogVo convertToLogVO(Log l) {
        return new LogVo(l.getTrackId(),l.getOrderType(),l.getLogDetail(),l.getLogType(),l.getLogTime(),l.getCode());
    }

    private void positionHandle(JSONObject positionObject, String key) {
        String content = FileUtil.readUtf8String(positionObject.getString(key));
        JSONObject contentObject = JSON.parseObject(content);
        JSONArray pointsJsonArray = contentObject.getJSONArray("points");
        List<BigDecimal[]> chart1dataList = new ArrayList<>();
        List<BigDecimal> chart2dataList = new ArrayList<>();
        List<String> chart2utcList = new ArrayList<>();
        pointsJsonArray.forEach(o -> {
            JSONObject point = (JSONObject) o;
            BigDecimal[] pointArray = new BigDecimal[]{point.getBigDecimal("dt"), point.getBigDecimal("ds")};
            chart1dataList.add(pointArray);
            chart2dataList.add(point.getBigDecimal("dist"));
            chart2utcList.add(point.getString("utc"));
        });
        JSONObject output = new JSONObject();
        output.put("satid_p", contentObject.getIntValue("satid_p"));
        output.put("satid_s", contentObject.getIntValue("satid_s"));
        output.put("chart1", chart1dataList.toArray());
        JSONObject chart2 = new JSONObject();
        chart2.put("x", chart2utcList.toArray());
        chart2.put("y", chart2dataList.toArray());
        output.put("chart2", chart2);

        positionObject.put(key, output);
    }

    /**
     * 为code=151创建日志
     */
    private Log createPyLog151(Matcher m, String model, String logTrackId) {
        String logType = m.group(1);
        String logTime = m.group(2);
        String logCode = "151";
        String logDetails = m.group(3);
        return getLog(model, logTrackId, logType, logTime, logCode, logDetails);
    }

    /**
     * 为code=150创建日志
     */
    private Log createPyLog150(Matcher m, String model, String logTrackId) {
        String logType = m.group(1);
        String logTime = m.group(2);
        String logCode = "150";
        String logDetails = m.group(3);
        return getLog(model, logTrackId, logType, logTime, logCode, logDetails);
    }

    @NotNull
    private Log getLog(String model, String logTrackId, String logType, String logTime, String logCode, String logDetails) {
        Log logInfo = new Log();
        logInfo.setOrderType(model);
        logInfo.setCode(logCode);
        logInfo.setLogDetail(logDetails);
        logInfo.setLogTime(setUTCTime(logTime));
        logInfo.setLogType(logType);
        logInfo.setTrackId(logTrackId);
        return logInfo;
    }

    private Log createPyLog(Matcher m, String model, String logTrackId) {
        String logType = m.group(1);
        String logTime = m.group(2);
        String logCode = m.group(3);
        String logDetails = m.group(4);
        return getLog(model, logTrackId, logType, logTime, logCode, logDetails);
    }


}
