package com.yhl.gj.config.pyconfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;

@Component
public class PyWorkDirConfig {

    @Value("${pyScript.usePoolVersion}")
    private  boolean usePoolVersion;
    @Value("${pyScript.pyWorkDir}")
    private String pyWorkDir;
    @Value("${pyScript.pyPoolWorkDir}")
    private String  pyPoolWorkDir;

    /**
     * py脚本工作目录
     */
    @Bean
    public Resource pyWorkDir() throws MalformedURLException {
        return new FileUrlResource(pyWorkDir);
    }

    @Bean
    public Resource pyPoolWorkDir() throws MalformedURLException {
        return new FileUrlResource(pyPoolWorkDir);
    }
}
