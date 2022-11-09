package com.yhl.gj;

import com.yhl.gj.service.CallWarningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 初始化是否
 */
@Order(1)
@Slf4j
//@Component
public class InitCompetition implements CommandLineRunner {
    @Value("${task.defaultParam.flushToDBOnStart}")
    private boolean flushDefaultConfigToDB;

    @Resource
    private CallWarningService callWarningService;

    @Override
    public void run(String... args) {
        if (flushDefaultConfigToDB) {
            log.info("------使用默认参数配置覆盖数据库中的配置------");
            callWarningService.flushDefaultConfigToDB();
        }
    }


}