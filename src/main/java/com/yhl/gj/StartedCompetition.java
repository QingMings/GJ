package com.yhl.gj;

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
@Component
public class StartedCompetition implements CommandLineRunner {

    @Override
    public void run(String... args) {

            log.info("------启动成功------");

    }


}