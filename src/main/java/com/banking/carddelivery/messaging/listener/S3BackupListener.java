package com.banking.carddelivery.messaging.listener;

import com.banking.carddelivery.messaging.event.DeliveryEvaluatedEvent;
import com.banking.carddelivery.publisher.BackupPublisher;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class S3BackupListener {

    private static final Logger log = LoggerFactory.getLogger(S3BackupListener.class);

    private final BackupPublisher backupPublisher;

    public S3BackupListener(BackupPublisher backupPublisher) {
        this.backupPublisher = backupPublisher;
    }

    @RabbitListener(queues = "${carddelivery.rabbitmq.queues.s3}")
    public void onMessage(DeliveryEvaluatedEvent event, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.debug("Iniciando backup S3 para eventId={}", event.getEventId());
            backupPublisher.publicar(event);
            channel.basicAck(deliveryTag, false);
        } catch (Exception ex) {
            log.error("Erro ao publicar backup S3 para eventId={}. Mensagem será reenfileirada.", event.getEventId(), ex);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}

