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

@Component
@Order(2)
public class BlackCardExpressEvaluator implements DeliveryEvaluator {

    private static final int BLACK_CARD_PRAZO_DIAS = 2;

    @Override
    public boolean aplicaPara(CardType cardType, RegiaoAtendimento regiao) {
        return CardType.BLACK == cardType
                && Boolean.TRUE.equals(regiao.getPermiteEntrega())
                && !PerfilRisco.ALTO.name().equals(regiao.getPerfilRisco());
    }

    @Override
    public DeliveryEvaluationResponse avaliar(String cep, CardType cardType, EnderecoDTO endereco, RegiaoAtendimento regiao) {
        return DeliveryEvaluationResponse.builder()
                .cep(cep)
                .endereco(endereco)
                .decisao(DeliveryDecision.APROVADO)
                .modalidade(Modalidade.TRANSPORTADORA_SEGURA)
                .transportadora(regiao.getTransportadora() != null ? regiao.getTransportadora() : "EXPRESS_CARD")
                .prazoDiasUteis(BLACK_CARD_PRAZO_DIAS)
                .rastreavel(true)
                .exigeAssinatura(true)
                .riscoRegiao(PerfilRisco.valueOf(regiao.getPerfilRisco()))
                .consultadoEm(LocalDateTime.now())
                .build();
    }
}
