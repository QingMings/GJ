package com.yhl.gj.component;

import cn.hutool.core.lang.Assert;
import com.rabbitmq.client.Channel;
import com.yhl.gj.commons.constant.QueuesConstants;
import com.yhl.gj.model.Task;
import com.yhl.gj.service.CallWarningService;
import com.yhl.gj.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
//@Component
public class TaskTrigger {


    @Resource
    private CallWarningService callWarningService;
    @Resource
    private TaskService taskService;

    @RabbitListener(bindings = @QueueBinding(exchange = @Exchange(value = QueuesConstants.TASK_TRIGGER_EXCHANGE, type = "direct"),
            value = @Queue(value = QueuesConstants.TASK_TRIGGER_QUEUE, durable = "true"),
            key = QueuesConstants.TASK_TRIGGER_ROUTE_KEY))
    @RabbitHandler
    public void taskTrigger(String msg, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            log.info("===========task trigger by mq :{}===========", msg);
            Assert.notNull(msg,"mq task trigger taskId must not null");
            Task task = taskService.getById(msg);
            Assert.notNull(task,"mq task trigger 根据taskId 未找到数据");
            callWarningService.executeTask(task,null);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            e.printStackTrace();
            channel.basicReject(tag, false);
        }
    }
}
