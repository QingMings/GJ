package com.yhl.gj.websocket;

/**
 * socket 配置 常量
 */
public interface SocketConstants {
    /**
     * socket 端点
     */
    String SOCKET_ENDPOINT = "/stomp/websocketJs";
    /**
     * 配置客户端接收一对一小的的前缀
     */
    String SOCKET_USER_PREFIX = "/user";
    /**
     * 应用服务器浅醉
     * 表示所有以 app 开头 的客户端消息或请求都会路由到 @MessageMapping 注解的方法中
     */
    String SOCKET_APP_PREFIX="/app";

    //------------------------------------
    /**
     * 连接 kafka 给前端推消息的前缀 <br>
     *
     * <code>
     *     // 示例 <br>
     *     '/queue/isim/1234323435435'
     * </code>
     */
    String SUB_ADDR_ISIM_PREFIX= "/queue/isim/";

    /**
     *  给前端播放回放消息的前缀
     */
    String SUB_ADDR_PLAYBACK_PREFIX ="/queue/playback";
}
