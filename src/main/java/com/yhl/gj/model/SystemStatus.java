package com.yhl.gj.model;

public class SystemStatus {
    public SystemStatus() {
    }

    public SystemStatus(String systemCode, String serviceName, Integer serviceStatus, Integer des, String statusTime, String ip) {
        this.systemCode = systemCode;
        this.serviceName = serviceName;
        this.serviceStatus = serviceStatus;
        this.des = des;
        this.statusTime = statusTime;
        this.ip = ip;
    }

    private String systemCode;
    private String serviceName;
    private Integer serviceStatus;
    private Integer des;
    private String statusTime;
    private String ip;

    public String getSystemCode() {
        return systemCode;
    }

    public void setSystemCode(String systemCode) {
        this.systemCode = systemCode;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Integer getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(Integer serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    public Integer getDes() {
        return des;
    }

    public void setDes(Integer des) {
        this.des = des;
    }

    public String getStatusTime() {
        return statusTime;
    }

    public void setStatusTime(String statusTime) {
        this.statusTime = statusTime;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
