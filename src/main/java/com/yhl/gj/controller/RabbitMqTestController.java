package com.yhl.gj.controller;

import com.yhl.gj.commons.base.Response;
import com.yhl.gj.commons.constant.QueuesConstants;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/rabbitMq")
public class RabbitMqTestController {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @PostMapping("/send")
    public Response sendMessage(@RequestParam("name") String name) {
        rabbitTemplate.convertAndSend(QueuesConstants.SYS_LOG_ADD_EXCHANGE, QueuesConstants.SYS_LOG_ADD_ROUTE_KEY, name);
        return Response.buildSucc();
    }
}
