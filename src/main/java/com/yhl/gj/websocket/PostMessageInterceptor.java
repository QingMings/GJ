package com.yhl.gj.websocket;

import cn.hutool.core.util.ObjectUtil;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;


public class PostMessageInterceptor implements ChannelInterceptor {

    /**
     * 发送消息后的一下处理逻辑
     * @param message
     * @param channel
     * @param sent
     */
    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        SimpMessageHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,SimpMessageHeaderAccessor.class);
        if (ObjectUtil.isEmpty(accessor)){
            return;
        }

        if (SimpMessageType.CONNECT.equals(accessor.getMessageType())){

        }
        if (SimpMessageType.MESSAGE.equals(accessor.getMessageType())){

        }
    }
}
