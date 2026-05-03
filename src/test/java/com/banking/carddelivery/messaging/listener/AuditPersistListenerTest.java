package com.banking.carddelivery.messaging.listener;

import com.banking.carddelivery.domain.entity.AuditoriaConsulta;
import com.banking.carddelivery.domain.enums.CardType;
import com.banking.carddelivery.domain.enums.DeliveryDecision;
import com.banking.carddelivery.domain.enums.Modalidade;
import com.banking.carddelivery.domain.enums.PerfilRisco;
import com.banking.carddelivery.messaging.event.DeliveryEvaluatedEvent;
import com.banking.carddelivery.service.audit.AuditWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditPersistListenerTest {

    @Mock private AuditWriter auditWriter;
    @Mock private ObjectMapper objectMapper;
    @Mock private Channel channel;
    @Mock private Message message;
    @Mock private MessageProperties messageProperties;

    private AuditPersistListener listener;

    @BeforeEach
    void setUp() throws Exception {
        listener = new AuditPersistListener(auditWriter, objectMapper);
        when(message.getMessageProperties()).thenReturn(messageProperties);
        when(messageProperties.getDeliveryTag()).thenReturn(1L);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"eventId\":\"test\"}");
    }

    @Test
    void onMessage_success_savesAndAcks() throws IOException {
        DeliveryEvaluatedEvent event = buildEvent();

        listener.onMessage(event, message, channel);

        verify(auditWriter).salvar(any(AuditoriaConsulta.class));
        verify(channel).basicAck(1L, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void onMessage_success_buildsCorrectAuditoria() throws IOException {
        DeliveryEvaluatedEvent event = buildEvent();
        ArgumentCaptor<AuditoriaConsulta> captor = ArgumentCaptor.forClass(AuditoriaConsulta.class);

        listener.onMessage(event, message, channel);

        verify(auditWriter).salvar(captor.capture());
        AuditoriaConsulta saved = captor.getValue();
        assertThat(saved.getEventId()).isEqualTo("test-event-id");
        assertThat(saved.getCepConsultado()).isEqualTo("01310100");
        assertThat(saved.getCustomerId()).isEqualTo("C123456");
        assertThat(saved.getCardType()).isEqualTo("DEBIT");
        assertThat(saved.getDecisao()).isEqualTo("APROVADO");
        assertThat(saved.getStatus()).isEqualTo("SUCESSO");
    }

    @Test
    void onMessage_withNullOptionalFields_buildsAuditoriaWithNulls() throws IOException {
        DeliveryEvaluatedEvent event = DeliveryEvaluatedEvent.builder()
                .eventId("test-event-id")
                .cep("01310100")
                .customerId("C123456")
                .cardType(null)
                .decisao(null)
                .modalidade(null)
                .riscoRegiao(null)
                .ocorridoEm(LocalDateTime.now())
                .status("SUCESSO")
                .build();
        ArgumentCaptor<AuditoriaConsulta> captor = ArgumentCaptor.forClass(AuditoriaConsulta.class);

        listener.onMessage(event, message, channel);

        verify(auditWriter).salvar(captor.capture());
        assertThat(captor.getValue().getCardType()).isNull();
        assertThat(captor.getValue().getDecisao()).isEqualTo("DESCONHECIDO");
        assertThat(captor.getValue().getModalidade()).isNull();
    }

    @Test
    void onMessage_exception_nacksMessage() throws IOException {
        DeliveryEvaluatedEvent event = buildEvent();
        doThrow(new RuntimeException("DB error")).when(auditWriter).salvar(any());

        listener.onMessage(event, message, channel);

        verify(channel).basicNack(1L, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    private DeliveryEvaluatedEvent buildEvent() {
        return DeliveryEvaluatedEvent.builder()
                .eventId("test-event-id")
                .cep("01310100")
                .customerId("C123456")
                .cardType(CardType.DEBIT)
                .ocorridoEm(LocalDateTime.of(2024, 6, 15, 10, 30))
                .decisao(DeliveryDecision.APROVADO)
                .modalidade(Modalidade.CORREIOS)
                .riscoRegiao(PerfilRisco.BAIXO)
                .status("SUCESSO")
                .tempoRespostaMs(100L)
                .build();
    }
}
