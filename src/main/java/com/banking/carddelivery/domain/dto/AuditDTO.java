package com.banking.carddelivery.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditDTO {
    private Long id;
    private String eventId;
    private String cepConsultado;
    private String customerId;
    private String cardType;
    private LocalDateTime dataHora;
    private String decisao;
    private String modalidade;
    private Integer prazoDias;
    private String riscoRegiao;
    private String status;
    private Long tempoRespostaMs;
}
