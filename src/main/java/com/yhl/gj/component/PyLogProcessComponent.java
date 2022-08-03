package com.yhl.gj.component;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.yhl.gj.commons.constant.PyLogType;
import com.yhl.gj.model.Log;
import com.yhl.gj.service.CallWarningService;
import com.yhl.gj.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public void pythonPrintProcess(String pyLog, String model) {
        log.info(pyLog);
        Matcher m = pyLogRegexPattern.matcher(pyLog);
        if (!m.find()){
            log.error(pyLog);
            return;
        }
        String logType = m.group(1);
        String logTime = m.group(2);
        String logCode = m.group(3);
        String logDetails = m.group(4);
        Log logInfo = new Log();
        logInfo.setOrderType(model);
        logInfo.setLogDetail(logDetails);
        LocalDateTime localDateTime = DateUtil.parseLocalDateTime(logTime, "yyyy-MM-dd HH:mm:ss.SSSSSS");
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
        Instant instant = zonedDateTime.toInstant();;
        Date date = Date.from(instant);
        logInfo.setLogTime(new Timestamp(date.getTime()));
        logInfo.setLogType(logType);
        logInfo.setTrackId("100");
        logService.save(logInfo);

        switch (StrUtil.trim(logType)){
            case PyLogType.PROGRESS:
                log.info("{}--{}",logType,logDetails);
                break;
            case PyLogType.RESULT:
                log.info("{}--{}",logType,logDetails);
                break;
            case PyLogType.ERROR:
//                log.error("{}--{}",logType,logDetails);
                break;
            default:
                log.info(logDetails);
        }
    }
    private void saveLogToDB(){

    }
}
