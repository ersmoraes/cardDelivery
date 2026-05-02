package com.banking.carddelivery.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RabbitMQConfig {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQConfig.class);

    @Value("${carddelivery.rabbitmq.exchange}")
    private String exchange;

    @Value("${carddelivery.rabbitmq.dlx}")
    private String dlx;

    @Value("${carddelivery.rabbitmq.queues.persist}")
    private String queuePersist;

    @Value("${carddelivery.rabbitmq.queues.s3}")
    private String queueS3;

    @Value("${carddelivery.rabbitmq.queues.dlq}")
    private String queueDlq;

    @Value("${carddelivery.rabbitmq.routing-keys.delivery-evaluated}")
    private String routingKeyDeliveryEvaluated;

    @Bean
    public TopicExchange cardsExchange() {
        return ExchangeBuilder.topicExchange(exchange).durable(true).build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(dlx).durable(true).build();
    }

    @Bean
    public Queue auditPersistQueue() {
        return QueueBuilder.durable(queuePersist)
                .withArguments(Map.of(
                        "x-dead-letter-exchange", dlx,
                        "x-dead-letter-routing-key", queueDlq))
                .build();
    }

    @Bean
    public Queue auditS3Queue() {
        return QueueBuilder.durable(queueS3)
                .withArguments(Map.of(
                        "x-dead-letter-exchange", dlx,
                        "x-dead-letter-routing-key", queueDlq))
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(queueDlq).build();
    }

    @Bean
    public Binding bindingPersist(Queue auditPersistQueue, TopicExchange cardsExchange) {
        return BindingBuilder.bind(auditPersistQueue)
                .to(cardsExchange)
                .with(routingKeyDeliveryEvaluated);
    }

    @Bean
    public Binding bindingS3(Queue auditS3Queue, TopicExchange cardsExchange) {
        return BindingBuilder.bind(auditS3Queue)
                .to(cardsExchange)
                .with(routingKeyDeliveryEvaluated);
    }

    @Bean
    public Binding bindingDlq(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with(queueDlq);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setMandatory(true);

        //loga falha mas não bloqueia o fluxo principal
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.warn("Mensagem não confirmada pelo broker. Causa: {} correlationData={}", cause, correlationData);
            }
        });

        //mensagem não roteável
        template.setReturnsCallback(returned ->
                log.warn("Mensagem retornada pelo broker. exchange={} routingKey={} replyCode={} replyText={}",
                        returned.getExchange(), returned.getRoutingKey(),
                        returned.getReplyCode(), returned.getReplyText()));

        return template;
    }
}
