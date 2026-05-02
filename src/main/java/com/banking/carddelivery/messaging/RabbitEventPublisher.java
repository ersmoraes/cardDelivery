package com.banking.carddelivery.messaging;

import com.banking.carddelivery.messaging.event.DeliveryEvaluatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RabbitEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public RabbitEventPublisher(RabbitTemplate rabbitTemplate,
                                @Value("${carddelivery.rabbitmq.exchange}") String exchange,
                                @Value("${carddelivery.rabbitmq.routing-keys.delivery-evaluated}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    @Override
    public void publicar(DeliveryEvaluatedEvent event) {
        log.debug("Publicando evento eventId={} na exchange={} routingKey={}",
                event.getEventId(), exchange, routingKey);
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
        log.info("Evento publicado com sucesso: eventId={}", event.getEventId());
    }
}
