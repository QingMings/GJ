package com.yhl.gj.config.pyconfig;

import cn.hutool.core.io.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Paths;

@Component
public class OrderRunParamConfig {
    @Value("${orderRunParamSavePath}")
    private String orderRunParamSavePath;


    public void writeRunParams(String fileName, String content) throws IOException {
        Resource resource = new FileUrlResource(orderRunParamSavePath.concat("/").concat(fileName).concat(".json"));
        FileUtil.writeUtf8String(content,resource.getFile());
    }
}
