//package com.yhl.gj.websocket;
//
//import cn.hutool.core.collection.CollectionUtil;
//import cn.hutool.core.lang.Assert;
////import com.yhl.service.KafkaService;
////import com.yhl.service.PlaybackService;
////import com.yhl.util.ConfigUtil;
////import com.yhl.vo.KafkaConsumerVo;
////import com.yhl.vo.PlaybackVo;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections4.CollectionUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.integration.kafka.dsl.Kafka;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.MessageChannel;
//import org.springframework.messaging.simp.stomp.StompCommand;
//import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
//import org.springframework.messaging.support.ChannelInterceptor;
//import org.springframework.messaging.support.MessageHeaderAccessor;
//
//import java.util.List;
//
///**
// * websocket 消息连接器，监听socket用户连接情况
// * 或访问header
// */
//@Slf4j
//public class WebSocketChannelInterceptor implements ChannelInterceptor {
//
////    @Autowired
////    private KafkaService kafkaService;
//
////    @Autowired
////    private PlaybackService playbackService;
//
//    /**
//     * 在消息发送前调用，如果返回值空，则不会调用实际的消息发送
//     *
//     * @param message
//     * @param channel
//     * @return
//     */
//    @Override
//    public Message<?> preSend(Message<?> message, MessageChannel channel) {
//        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
//        /**
//         * 1. 可以判断是否为首次连接，如果已连接，可以直接返回message
//         * 2. 也可以在这里封装用户信息
//         */
//        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
//            List<String> tokens = accessor.getNativeHeader("token");
//            List<String> sceneName = accessor.getNativeHeader("projectSceneName");
//            if (CollectionUtils.isNotEmpty(tokens)) {
//                /*
//                 * 1. 这里获得的就是 JS stompClient.connect(headers,function(frame){....}) 中的header 信息
//                 * 2. JS 中的 header 可以封装多个参数，格式是{key1: value1,key2:value2}
//                 * 3. header 中的key可以是一样的，取出来就是List
//                 */
//                String token = tokens.get(0);
//                CustomWebSocketUserAuthentication user = (CustomWebSocketUserAuthentication) accessor.getUser();
//                log.debug("首次连接，httpSession: {}", token);
//            }
//        }
//        /**
//         * 具体订阅的链接地址，根据不同的地址可以做不同的逻辑
//         */
//        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
//            log.debug(accessor.getDestination());
//            if (StringUtils.isNotEmpty(accessor.getDestination())
//                    && StringUtils.contains(accessor.getDestination(), SocketConstants.SUB_ADDR_ISIM_PREFIX)) {
//                String idString = StringUtils.replace(accessor.getDestination(),SocketConstants.SUB_ADDR_ISIM_PREFIX,"");
//                Assert.notNull(idString,"idString 不能为空");
//
//                CustomWebSocketUserAuthentication user = (CustomWebSocketUserAuthentication) accessor.getUser();
//                user.setIsimProId(idString);
////                KafkaConsumerVo kafkaConsumerVo = new KafkaConsumerVo();
////                kafkaConsumerVo.setGroupId(user.getName());
////                kafkaConsumerVo.setBootstrapServices(ConfigUtil.getKAFKAADDRESS());
////                kafkaConsumerVo.setTopicName(idString);
////                kafkaService.subscribe(kafkaConsumerVo); // 开始连接kafka,接收实时消息
//
//                // 省略保存场景信息到数据库的操作（回放列表）
//            }
//        }
//
//        if (StringUtils.isNotEmpty(accessor.getDestination())
//            && StringUtils.contains(accessor.getDestination(),SocketConstants.SUB_ADDR_PLAYBACK_PREFIX)){
//            String idString = StringUtils.replace(accessor.getDestination(),SocketConstants.SUB_ADDR_PLAYBACK_PREFIX,"");
//            Assert.notNull(idString,"idString 不能为空");
//            CustomWebSocketUserAuthentication user = (CustomWebSocketUserAuthentication) accessor.getUser();
//            user.setIsimProId(idString);
////            playbackService.startPlay(idString); // 开始回放
//        }
//        /**
//         * 断开连接，可以处理一下资源释放，清理的工作
//         */
//        if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
//            CustomWebSocketUserAuthentication user = (CustomWebSocketUserAuthentication) accessor.getUser();
//            log.debug("断开连接：{}",user.getName());
//            // idString 和 isimProId 是同一个东西，懒得改了
////            kafkaService.unSubscribe(user.getIsimProId());
////            playbackService.stopPlay(user.getIsimProId());
//        }
//
//        return message;
//    }
//}
