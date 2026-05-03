package com.banking.carddelivery.messaging;

import com.banking.carddelivery.messaging.event.DeliveryEvaluatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitEventPublisherTest {

    @Mock private RabbitTemplate rabbitTemplate;

    private RabbitEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new RabbitEventPublisher(rabbitTemplate, "cards.exchange", "delivery.evaluated");
    }

    @Test
    void publicar_sendsEventToRabbit() {
        DeliveryEvaluatedEvent event = DeliveryEvaluatedEvent.builder()
                .eventId("test-event-id")
                .cep("01310100")
                .build();

        publisher.publicar(event);

        verify(rabbitTemplate).convertAndSend("cards.exchange", "delivery.evaluated", event);
    }

    @Test
    void publicar_differentEvent_sendsCorrectly() {
        DeliveryEvaluatedEvent event = DeliveryEvaluatedEvent.builder()
                .eventId("other-event-id")
                .build();

        publisher.publicar(event);

        verify(rabbitTemplate).convertAndSend("cards.exchange", "delivery.evaluated", event);
    }
}
