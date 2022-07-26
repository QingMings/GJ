package com.yhl.gj.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Slf4j
@Configuration
public class DruidConfig {

    @PostConstruct
    public void setProperties(){
        log.info("关闭druid userPingMethod");
        System.setProperty("druid.mysql.usePingMethod","false");
    }

}
