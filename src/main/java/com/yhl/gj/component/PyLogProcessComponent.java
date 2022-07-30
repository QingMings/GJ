package com.yhl.gj.component;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.yhl.gj.commons.constant.PyLogType;
import com.yhl.gj.model.Log;
import com.yhl.gj.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.*;
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

    public void pythonPrintProcess(String pyLog,String model){
        Matcher m = pyLogRegexPattern.matcher(pyLog);
//        String[] logArray = pyLog.split("@");
//        if (logArray.length!=2){
//            log.error(pyLog);
//            return;
//
//        }
        if (!m.find()){
            log.error(pyLog);
            return;
        }
        String logType = m.group(1);
        String logTime = m.group(2);
        String logDetails = m.group(3);
        Log logInfo = new Log();
        logInfo.setOrderType(model);
        logInfo.setLogDetail(logDetails);
        LocalDateTime localDateTime = DateUtil.parseLocalDateTime(logTime, "yyyy-MM-dd HH:mm:ss.SSSSSS");
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
        Instant instant = zonedDateTime.toInstant();;
        Date date = Date.from(instant);

        System.out.println(logTime);

//        logInfo.setLogTime(date);
        System.out.println(DateUtil.format(logInfo.getLogTime(), "yyyy-MM-dd HH:mm:ss.SSSSSS"));
        logInfo.setLogType(logType);
        logInfo.setTrackId("100");
        logService.save(logInfo);

        switch (StrUtil.trim(logType)){
            case PyLogType.PROCESS:
                log.info("{}--{}",logType,logDetails);
                break;
            case PyLogType.RESULT:
                log.info("{}--{}",logType,logDetails);
                break;
            case PyLogType.ERROR:
                log.error("{}--{}",logType,logDetails);
                break;
            default:
                log.info(logDetails);
        }
    }
    private void saveLogToDB(){

    }
}
