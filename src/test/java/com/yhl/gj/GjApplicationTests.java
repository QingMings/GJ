package com.yhl.gj;

import com.yhl.gj.model.Log;
import com.yhl.gj.service.LogService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

@SpringBootTest
class GjApplicationTests {


    @Resource
    private LogService logService;

    @Test
    void contextLoads() {
    }

    @Test
    void testSaveLogWithMs(){
        Log logInfo = new Log();
        logInfo.setOrderType("Type");
        logInfo.setLogDetail("Detail");
        String dateStr = "2022-07-26 16:31:46.949673";
        DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()

                .appendPattern ("yyyy-MM-dd HH:mm:ss")

                .appendFraction ( ChronoField.NANO_OF_SECOND , 0 , 9 , true ) // 纳秒 = 0-9 位小数秒。

                .toFormatter();
        LocalDateTime localDateTime  = LocalDateTime.parse ( dateStr , dateTimeFormatter );
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("UTC+8"));
        Instant instant = zonedDateTime.toInstant();;
        Timestamp ts = Timestamp.from(instant);

        System.out.println(dateStr);

        logInfo.setLogTime(ts);

        logInfo.setLogType("LogType");
        logInfo.setTrackId("100");
        logService.save(logInfo);
    }

}
