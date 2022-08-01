//package com.yhl.gj.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.messaging.simp.config.MessageBrokerRegistry;
//import org.springframework.web.socket.config.annotation.EnableWebSocket;
//import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
//import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
//import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
//
//@Configuration
//@EnableWebSocketMessageBroker
//public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
//    @Override
//    public void configureMessageBroker(MessageBrokerRegistry registry) {
//        registry.enableSimpleBroker("/topic","queue","/user"); //表示在topic和queue这两个域上服务端可以向客户端发消息
//        registry.setApplicationDestinationPrefixes("/app");//客户端向服务器端发送时的主题上面需要加"/app"作为前缀
//        registry.setUserDestinationPrefix("/user");   // 指定用户一对一的主题，前缀 '/user'
//
//    }
//
//    @Override
//    public void registerStompEndpoints(StompEndpointRegistry registry) {
//        registry.addEndpoint("/stomp/endpoint")
//                .setAllowedOrigins("*")
//                .withSockJS();
//    }
//
//
//
//}
