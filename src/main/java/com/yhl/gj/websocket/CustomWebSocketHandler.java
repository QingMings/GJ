//package com.yhl.gj.websocket;
//
//import cn.hutool.core.util.ObjectUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.web.socket.CloseStatus;
//import org.springframework.web.socket.WebSocketHandler;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
//import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;
//@Slf4j
//public class CustomWebSocketHandler implements WebSocketHandlerDecoratorFactory {
//    @Autowired
//    private StringRedisTemplate stringRedisTemplate;
//    @Override
//    public WebSocketHandler decorate(WebSocketHandler webSocketHandler) {
//
//        return new WebSocketHandlerDecorator(webSocketHandler){
//            @Override
//            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//                if (ObjectUtil.isNotNull(session.getPrincipal())){
//                    String uid = session.getPrincipal().getName();
//                    log.info("用户 {} 登录",uid);
//                    stringRedisTemplate.opsForSet().add("online",uid);
//                }
//                super.afterConnectionEstablished(session);
//            }
//
//            @Override
//            public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
//                if (ObjectUtil.isNotNull(session.getPrincipal())){
//                    String uid = session.getPrincipal().getName();
//                    log.info("用户 {} 退出",uid);
//                    stringRedisTemplate.opsForSet().remove("online",uid);
//                }
//                super.afterConnectionClosed(session, closeStatus);
//            }
//        };
//    }
//}
