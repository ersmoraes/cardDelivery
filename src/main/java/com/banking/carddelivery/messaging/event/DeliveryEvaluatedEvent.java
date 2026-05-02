package com.banking.carddelivery.messaging.event;

import com.banking.carddelivery.domain.dto.EnderecoDTO;
import com.banking.carddelivery.domain.enums.CardType;
import com.banking.carddelivery.domain.enums.DeliveryDecision;
import com.banking.carddelivery.domain.enums.Modalidade;
import com.banking.carddelivery.domain.enums.PerfilRisco;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryEvaluatedEvent {

    private String eventId;
    private String cep;
    private String customerId;
    private CardType cardType;
    private LocalDateTime ocorridoEm;
    private EnderecoDTO endereco;
    private DeliveryDecision decisao;
    private Modalidade modalidade;
    private Integer prazoDiasUteis;
    private PerfilRisco riscoRegiao;
    private Long tempoRespostaMs;
    private String status;
}

