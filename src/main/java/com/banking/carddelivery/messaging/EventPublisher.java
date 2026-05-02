package com.banking.carddelivery.messaging;

import com.banking.carddelivery.messaging.event.DeliveryEvaluatedEvent;

public interface EventPublisher {

    void publicar(DeliveryEvaluatedEvent event);
}
