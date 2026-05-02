package com.banking.carddelivery.service.evaluator;

import com.banking.carddelivery.domain.dto.DeliveryEvaluationResponse;
import com.banking.carddelivery.domain.dto.EnderecoDTO;
import com.banking.carddelivery.domain.entity.RegiaoAtendimento;
import com.banking.carddelivery.domain.enums.CardType;
import com.banking.carddelivery.domain.enums.DeliveryDecision;
import com.banking.carddelivery.domain.enums.Modalidade;
import com.banking.carddelivery.domain.enums.PerfilRisco;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

@Component
@Order(3)
public class StandardCardEvaluator implements DeliveryEvaluator {

    private static final Set<CardType> STANDARD_TYPES = Set.of(CardType.DEBIT, CardType.CREDIT, CardType.MULTIPLE);

    @Override
    public boolean aplicaPara(CardType cardType, RegiaoAtendimento regiao) {
        return STANDARD_TYPES.contains(cardType) && Boolean.TRUE.equals(regiao.getPermiteEntrega());
    }

    @Override
    public DeliveryEvaluationResponse avaliar(String cep, CardType cardType, EnderecoDTO endereco, RegiaoAtendimento regiao) {
        PerfilRisco risco = PerfilRisco.valueOf(regiao.getPerfilRisco());
        Modalidade modalidade = risco == PerfilRisco.BAIXO ? Modalidade.CORREIOS : Modalidade.TRANSPORTADORA_SEGURA;

        return DeliveryEvaluationResponse.builder()
                .cep(cep)
                .endereco(endereco)
                .decisao(DeliveryDecision.APROVADO)
                .modalidade(modalidade)
                .transportadora(regiao.getTransportadora())
                .prazoDiasUteis(regiao.getPrazoDias())
                .rastreavel(true)
                .exigeAssinatura(modalidade == Modalidade.TRANSPORTADORA_SEGURA)
                .riscoRegiao(risco)
                .consultadoEm(LocalDateTime.now())
                .build();
    }
}

