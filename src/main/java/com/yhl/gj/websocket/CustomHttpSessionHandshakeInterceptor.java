package com.yhl.gj.websocket;

import cn.hutool.core.util.ObjectUtil;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

/**
 * sockJs 解析 queryString 设置到 attribute 中
 */
public class CustomHttpSessionHandshakeInterceptor extends HttpSessionHandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        ServletServerHttpRequest servletServerHttpRequest = (ServletServerHttpRequest) request;
        String token = servletServerHttpRequest.getServletRequest().getParameter("token");
        String projectSceneName = servletServerHttpRequest.getServletRequest().getParameter("projectSceneName");
        // 在握手之前从httpRequest中读取 queryParam 设置到 attribute 中。
        if (ObjectUtil.isNotEmpty(token)){
            attributes.put("token",token);
        }
        if (ObjectUtil.isNotEmpty(projectSceneName)){
            attributes.put("projectSceneName",projectSceneName);
        }
        return super.beforeHandshake(request, response, wsHandler, attributes);
    }
}
