package com.yhl.gj.config;

import com.yhl.gj.commons.constant.QueuesConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.ContentTypeDelegatingMessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;


@Configuration
@Slf4j
public class RabbitConfig {

    @Resource
    private ConnectionFactory connectionFactory;


    /**
     * 定制化amqp模版
     * <p>
     * ConfirmCallback接口用于实现消息发送到RabbitMQ交换器后接收ack回调   即消息发送到exchange  ack
     * ReturnCallback接口用于实现消息发送到RabbitMQ 交换器，但无相应队列与交换器绑定时的回调  即消息发送不到任何一个队列中  ack
     */
    @Bean
    public RabbitTemplate rabbitTemplate(MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        // 消息发送失败返回到队列中, yml需要配置 publisher-returns: true
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setMessageConverter(messageConverter);

        // 消息返回, yml需要配置 publisher-returns: true
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
        });

        // 消息确认, yml需要配置 publisher-confirms: true
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
            } else {
                log.debug("消息发送到exchange失败,原因: {}", cause);
            }
        });
        return rabbitTemplate;
    }

    @Bean
    public MessageConverter messageConverter() {
        return new ContentTypeDelegatingMessageConverter(new Jackson2JsonMessageConverter());
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return factory;
    }

    @Bean
    public DirectExchange sysLogExchange() {
        return (DirectExchange) ExchangeBuilder.directExchange(QueuesConstants.SYS_LOG_ADD_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue sysLogQueue() {
        return QueueBuilder.durable(QueuesConstants.SYS_LOG_ADD_QUEUE).build();
    }

    @Bean
    public Binding sysLogBinding() {
        return BindingBuilder.bind(sysLogQueue()).to(sysLogExchange()).with(QueuesConstants.SYS_LOG_ADD_ROUTE_KEY);
    }

    @Bean
    public DirectExchange warnReportExchange() {
        return (DirectExchange) ExchangeBuilder.directExchange(QueuesConstants.WARN_REPORT_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue warnReportQueue() {
        return QueueBuilder.durable(QueuesConstants.WARN_REPORT_QUEUE).build();
    }

    @Bean
    public Binding warnReportBinding() {
        return BindingBuilder.bind(warnReportQueue()).to(warnReportExchange()).with(QueuesConstants.WARN_REPORT_ROUTE_KEY);
    }




}
