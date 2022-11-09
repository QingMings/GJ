package com.yhl.gj.config.commonBase;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CommonBaseConfig {
    @Value("${loginConfig.loginPath}")
    private String loginPath;
    @Value("${loginConfig.logOutPath}")
    private String logOutPath;
    @Value("${loginConfig.verifyUser}")
    private String verifyUser;
    @Value("${loginConfig.systemStatus}")
    private String systemStatus;
    @Value("${loginConfig.logReport}")
    private String logReport;

    @Value("${serverConfig.systemCode}")
    private String serviceCode;
    @Value("${serverConfig.serviceName}")
    private String serviceName;

    public String getServiceCode() {
        return serviceCode;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getLoginPath() {
        return loginPath;
    }

    public String getLogOutPath() {
        return logOutPath;
    }

    public String getVerifyUser() {
        return verifyUser;
    }

    public String getSystemStatus() {
        return systemStatus;
    }

    public String getLogReport() {
        return logReport;
    }
}

