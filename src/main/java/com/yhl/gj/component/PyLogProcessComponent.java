package com.yhl.gj.component;

import cn.hutool.core.util.StrUtil;
import com.yhl.gj.commons.constant.PyLogType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Slf4j
@Component
public class PyLogProcessComponent {

    @Resource
    private Pattern pyLogRegexPattern;

    public void pythonPrintProcess(String pyLog){
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
}
