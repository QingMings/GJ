package com.yhl.gj.config.pyconfig;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
@Component
public class CLibParamJsonConfig {
    @Value("${pyConfig.c_lib_param_path}")
    private String cLibParamPath;

    @Bean
    private JSONObject clibParam() throws IOException {
        Resource resource = new FileUrlResource(cLibParamPath);
        File warningGatesFile = FileUtil.file(resource.getFile());
        String jsonStr = FileUtil.readUtf8String(warningGatesFile);
        return JSONObject.parseObject(jsonStr);
    }
}
