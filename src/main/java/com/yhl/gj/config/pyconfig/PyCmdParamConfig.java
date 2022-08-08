package com.yhl.gj.config.pyconfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PyCmdParamConfig {

    @Value("#{'${pyCmd.param_list}'.split(',')}")
    private List<String> cmd;
    @Value("#{'${pyCmd.param_pool_list}'.split(',')}")
    private List<String> poolCmd;

    public List<String> getCmd() {
        return cmd;
    }


    public List<String> getPoolCmd() {
        return poolCmd;
    }
}
