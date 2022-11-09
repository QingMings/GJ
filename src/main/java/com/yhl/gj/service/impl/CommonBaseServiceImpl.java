package com.yhl.gj.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.net.Ipv4Util;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.support.http.util.IPAddress;
import com.alibaba.fastjson.JSONObject;
import com.yhl.gj.commons.base.Response;
import com.yhl.gj.config.commonBase.CommonBaseConfig;
import com.yhl.gj.model.SystemStatus;
import com.yhl.gj.model.User;
import com.yhl.gj.service.CommonBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

@Service
public class CommonBaseServiceImpl implements CommonBaseService {

    @Resource
    private CommonBaseConfig baseConfig;
    @Resource
    private RestTemplate restTemplate;
    @Override
    public Response userLogin(String username, String password) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        User user = new User(username,password);
        HttpEntity<User> entity = new HttpEntity<User>(user,headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(baseConfig.getLoginPath(),entity,String.class);
        String responseContent = responseEntity.getBody();
        JSONObject responseToJson = JSONObject.parseObject(responseContent);
        if ("200".equals(responseToJson.getString("code"))){
            return Response.buildSucc(responseToJson.getJSONObject("data"));
        }else {
            return Response.buildFail(responseToJson.getInteger("code"),responseToJson.getString("msg"));
        }
    }

    @Override
    public Response userLogout(String token) {
        String url = StrUtil.concat(true,baseConfig.getVerifyUser(),"?token={1}");
        ResponseEntity<String> responseEntity =  restTemplate.getForEntity(baseConfig.getVerifyUser(),String.class,token);
        String responseContent = responseEntity.getBody();
        JSONObject responseToJson = JSONObject.parseObject(responseContent);
        if ("200".equals(responseToJson.getString("code"))){
            return Response.buildSucc();
        }else {
            return Response.buildFail(responseToJson.getInteger("code"),responseToJson.getString("msg"));
        }
    }

    @Override
    public Response verifyUser(String token) {
       String url = StrUtil.concat(true,baseConfig.getVerifyUser(),"?token={1}");
       ResponseEntity<String> responseEntity = restTemplate.getForEntity(url,String.class,token);
       String responseContent = responseEntity.getBody();
        JSONObject responseToJson = JSONObject.parseObject(responseContent);
        if ("200".equals(responseToJson.getString("status"))){
            return  Response.buildSucc(responseToJson.getJSONObject("data"));
        }else {
            return Response.buildFail(responseToJson.getInteger("status"),responseToJson.getString("reason"));
        }
    }

    @Override
    public Response systemStatus() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        SystemStatus status = new SystemStatus();
        status.setSystemCode(baseConfig.getServiceCode());
        status.setServiceName(baseConfig.getServiceName());
        status.setStatusTime(DateUtil.now());
        status.setIp(NetUtil.getLocalhost().getHostAddress());
        status.setDes(0);
        status.setServiceStatus(0);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(baseConfig.getSystemStatus(), status, String.class);
        String responseContent = responseEntity.getBody();
        JSONObject responseToJson = JSONObject.parseObject(responseContent);
        if ("200".equals(responseToJson.getString("code"))){
            return Response.buildSucc();
        }else {
            return Response.buildFail(responseToJson.getInteger("code"),responseToJson.getString("msg"));

        }
    }

    @Override
    public Response logPathReport(String code, String timeStr, String logPath, String logType, String ip) {
        return null;
    }
}
