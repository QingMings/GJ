package com.yhl.gj.config.pyconfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class PyV3ParamConfig {

    @Value("#{'${pyCmd.paramV3_list}'.split(',')}")
    private List<String> cmdV3;
    public List<String> getCmdV3() {
        return cmdV3;
    }

    @Value("${pyScriptV3.taskParam.path}")
    private String  defaultParamPath;

    public String getDefaultParamPath() {
        return defaultParamPath;
    }
}
