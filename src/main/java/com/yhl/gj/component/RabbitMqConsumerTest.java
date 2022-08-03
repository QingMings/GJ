package com.yhl.gj.component;

import com.rabbitmq.client.Channel;
import com.yhl.gj.commons.constant.QueuesConstants;
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
    public void rabbitMqConsumerTest(String msg, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            log.info("==========={}===========", msg);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            e.printStackTrace();
            channel.basicReject(tag, false);
        }
    }
}
