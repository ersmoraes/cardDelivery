package com.banking.carddelivery.messaging.listener;

import com.banking.carddelivery.messaging.event.DeliveryEvaluatedEvent;
import com.banking.carddelivery.publisher.BackupPublisher;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3BackupListenerTest {

    @Mock private BackupPublisher backupPublisher;
    @Mock private Channel channel;
    @Mock private Message message;
    @Mock private MessageProperties messageProperties;

    private S3BackupListener listener;

    @BeforeEach
    void setUp() {
        listener = new S3BackupListener(backupPublisher);
        when(message.getMessageProperties()).thenReturn(messageProperties);
        when(messageProperties.getDeliveryTag()).thenReturn(2L);
    }

    @Test
    void onMessage_success_publishesAndAcks() throws IOException {
        DeliveryEvaluatedEvent event = buildEvent();

        listener.onMessage(event, message, channel);

        verify(backupPublisher).publicar(event);
        verify(channel).basicAck(2L, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void onMessage_backupFails_nacksMessage() throws IOException {
        DeliveryEvaluatedEvent event = buildEvent();
        doThrow(new RuntimeException("S3 error")).when(backupPublisher).publicar(event);

        listener.onMessage(event, message, channel);

        verify(channel).basicNack(2L, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    private DeliveryEvaluatedEvent buildEvent() {
        return DeliveryEvaluatedEvent.builder()
                .eventId("test-event-id")
                .cep("01310100")
                .build();
    }
}
