package com.yhl.gj;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;

@MapperScan(basePackages = "com.yhl.gj.mapper")
@SpringBootApplication(exclude = {
//        DataSourceAutoConfiguration.class,
        QuartzAutoConfiguration.class,
//        DruidDataSourceAutoConfigure.class
})
public class GjApplication {

    public static void main(String[] args) {
        SpringApplication.run(GjApplication.class, args);
    }

}
