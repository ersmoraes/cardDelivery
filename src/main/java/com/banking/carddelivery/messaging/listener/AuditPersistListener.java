package com.banking.carddelivery.messaging.listener;

import com.banking.carddelivery.domain.entity.AuditoriaConsulta;
import com.banking.carddelivery.messaging.event.DeliveryEvaluatedEvent;
import com.banking.carddelivery.service.audit.AuditWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuditPersistListener {

    private static final Logger log = LoggerFactory.getLogger(AuditPersistListener.class);

    private final AuditWriter auditWriter;
    private final ObjectMapper objectMapper;

    public AuditPersistListener(AuditWriter auditWriter, ObjectMapper objectMapper) {
        this.auditWriter = auditWriter;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "${carddelivery.rabbitmq.queues.persist}")
    public void onMessage(DeliveryEvaluatedEvent event, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.debug("Processando auditoria para eventId={}", event.getEventId());

            AuditoriaConsulta auditoria = AuditoriaConsulta.builder()
                    .eventId(event.getEventId())
                    .cepConsultado(event.getCep())
                    .customerId(event.getCustomerId())
                    .cardType(event.getCardType() != null ? event.getCardType().name() : null)
                    .dataHora(event.getOcorridoEm())
                    .respostaApi(toJson(event))
                    .decisao(event.getDecisao() != null ? event.getDecisao().name() : "DESCONHECIDO")
                    .modalidade(event.getModalidade() != null ? event.getModalidade().name() : null)
                    .prazoDias(event.getPrazoDiasUteis())
                    .riscoRegiao(event.getRiscoRegiao() != null ? event.getRiscoRegiao().name() : null)
                    .status(event.getStatus())
                    .tempoRespostaMs(event.getTempoRespostaMs())
                    .build();

            auditWriter.salvar(auditoria);
            channel.basicAck(deliveryTag, false);
        } catch (Exception ex) {
            log.error("Erro ao persistir auditoria para eventId={}. Mensagem será reenfileirada.", event.getEventId(), ex);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private String toJson(DeliveryEvaluatedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception ex) {
            log.warn("Falha ao serializar evento para JSON: {}", ex.getMessage());
            return "{}";
        }
    }
}
