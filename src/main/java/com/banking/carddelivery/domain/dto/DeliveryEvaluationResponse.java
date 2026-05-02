package com.banking.carddelivery.domain.dto;

import com.banking.carddelivery.domain.enums.DeliveryDecision;
import com.banking.carddelivery.domain.enums.Modalidade;
import com.banking.carddelivery.domain.enums.PerfilRisco;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryEvaluationResponse {

    private String cep;
    private EnderecoDTO endereco;
    private DeliveryDecision decisao;
    private Modalidade modalidade;
    private String transportadora;
    private Integer prazoDiasUteis;
    private Boolean rastreavel;
    private Boolean exigeAssinatura;
    private PerfilRisco riscoRegiao;
    private String motivo;
    private List<String> agenciasSugeridas;
    private LocalDateTime consultadoEm;
}
