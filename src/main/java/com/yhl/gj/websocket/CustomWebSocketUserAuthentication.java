package com.yhl.gj.websocket;

import java.security.Principal;

/**
 * 自定义的websocket登录对象
 */
public class CustomWebSocketUserAuthentication implements Principal {

    private String token; // 用户登录的token
    private String sceneName; // 场景名称
    private String isimProId;  // 想定启动后的kafka队列id,isim会将仿真数据写到队列中
    @Override
    public String getName() {
        return token;
    }

    public CustomWebSocketUserAuthentication() {
    }

    public CustomWebSocketUserAuthentication(String token) {
        this.token = token;
    }

    public CustomWebSocketUserAuthentication(String token, String sceneName) {
        this.token = token;
        this.sceneName = sceneName;
    }

    public String getSceneName() {
        return sceneName;
    }

    public String getIsimProId() {
        return isimProId;
    }

    public void setIsimProId(String isimProId) {
        this.isimProId = isimProId;
    }
}
