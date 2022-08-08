package com.yhl.gj.component;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.yhl.gj.commons.constant.QueuesConstants;
import com.yhl.gj.model.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class RabbitMqConsumerTest {

    @RabbitListener(bindings = @QueueBinding(exchange = @Exchange(value = QueuesConstants.SYS_LOG_ADD_EXCHANGE, type = "direct"),
            value = @Queue(value = QueuesConstants.SYS_LOG_ADD_QUEUE, durable = "true"),
            key = QueuesConstants.SYS_LOG_ADD_ROUTE_KEY))
    @RabbitHandler
    public void rabbitMqConsumerTest(JSONObject msg, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            log.info("====SYS_LOG======={}===========", msg);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            e.printStackTrace();
            channel.basicReject(tag, false);
        }
    }

    @RabbitListener(bindings = @QueueBinding(exchange = @Exchange(value = QueuesConstants.WARN_REPORT_EXCHANGE, type = "direct"),
            value = @Queue(value = QueuesConstants.WARN_REPORT_QUEUE, durable = "true"),
            key = QueuesConstants.WARN_REPORT_ROUTE_KEY))
    @RabbitHandler
    public void rabbitMqConsumer_WARN_REPORT(JSONObject detail, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            log.info("====WARN_REPORT======={}===========", detail);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            e.printStackTrace();
            channel.basicReject(tag, false);
        }
    }
}
