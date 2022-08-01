package com.yhl.gj.config.pyconfig;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class PyInputFileSuffixConfig {

    // 卫星轨道文件后缀
    @Value("${suffixFilter.satelliteFile}")
    private String satelliteFile;
    // 目标轨道数据后缀
    @Value("${suffixFilter.targetOrbit}")
    private String targetOrbit;
    // 雷达输入文件后缀
    @Value("${suffixFilter.targetRadar}")
    private String targetRadar;
    // 激光输入文件后缀
    @Value("${suffixFilter.targetLaser}")
    private String targetLaser;
    @Value("${suffixFilter.obs_GTW}")
    private String obs_GTW;
    @Value("${suffixFilter.obs_EPH}")
    private String obs_EPH;

}
