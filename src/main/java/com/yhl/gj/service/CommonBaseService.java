package com.yhl.gj.service;

import com.yhl.gj.commons.base.Response;

public interface CommonBaseService {
    /**
     * 登录接口
     * @param username
     * @param password
     * @return
     */
    public Response  userLogin(String username,String password);

    /**
     * 登出接口
     * @param token
     * @return
     */
    public Response  userLogout(String token);

    /**
     * 用户信息获取接口
     * @param token
     * @return
     */
    public Response  verifyUser(String token);

    /**
     * 系统状态上报  POST
     * @return
     */
    public  Response systemStatus();

    /**
     * 日志路径上报
     * @return
     */
    public Response  logPathReport(String code,String timeStr,String logPath,String logType,String ip);
}
