package com.yhl.gj.config.pyconfig;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

// 配置文件默认值注入
//@Component
//public class WarningGateJsonConfig {
//    @Value("${pyConfig.warning_gates_path}")
//    private String warningGatesPath;
//
//    @Bean
//    private JSONObject warningGates() throws IOException {
//        Resource resource = new FileUrlResource(warningGatesPath);
//        File warningGatesFile = FileUtil.file(resource.getFile());
//        String jsonStr = FileUtil.readUtf8String(warningGatesFile);
//        return JSONObject.parseObject(jsonStr);
//    }
//}
