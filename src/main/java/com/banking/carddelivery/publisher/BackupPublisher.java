package com.banking.carddelivery.publisher;

import com.banking.carddelivery.messaging.event.DeliveryEvaluatedEvent;

public interface BackupPublisher {

    void publicar(DeliveryEvaluatedEvent event);
}
