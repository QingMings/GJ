package com.yhl.gj.config.pyconfig;

import cn.hutool.core.util.StrUtil;
import com.yhl.gj.commons.constant.PyLogType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//@Component
public class PyLogRegexConfig {
    @Value("${pyLog.regex_pattern}")
    private String regexPattern;

    @Value("${pyLog.warn_report_regex_pattern}")
    private String warnReportRegexPattern;

    @Value("${pyLog.max_warn_level_regex_pattern}")
    private String maxWarnLevelRegexPattern;

    @Bean
    public Pattern pyLogRegexPattern() {
        return Pattern.compile(regexPattern);
    }
    @Bean
    public Pattern warnReportRegexPattern() {
        return Pattern.compile(warnReportRegexPattern);
    }

    @Bean
    public Pattern maxWarnLevelRegexPattern() {
        return Pattern.compile(maxWarnLevelRegexPattern);
    }
}
