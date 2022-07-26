package com.yhl.gj.websocket;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * 自定义的websocket握手鉴权
 * sockJs 访问时候，通过url传参传递token或其他参数 <br>
 * <code>
 *   // javascript code <br>
 *  let socket = new SockJs('http://localhost:8009/stomp/websocketJs?token=xxxxx&projectSceneName=ddddsss');
 * </code>
 */
@Slf4j
public class CustomPrincipalHandShakeHandler extends DefaultHandshakeHandler {

    public static final String DEFAULT_SCENE_NAME= "未命名场景";
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = (String) attributes.get("token");
        if (StringUtils.isEmpty(token)){
            log.error("未登录系统，禁止使用websocket");
            return null;
        }
        String sceneName = StringUtils.defaultIfEmpty((String) attributes.get("projectSceneName"),DEFAULT_SCENE_NAME);
        return new CustomWebSocketUserAuthentication(token,sceneName);
    }
}
