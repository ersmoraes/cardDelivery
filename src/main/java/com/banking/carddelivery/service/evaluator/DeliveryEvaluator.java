package com.banking.carddelivery.service.evaluator;

import com.banking.carddelivery.domain.dto.DeliveryEvaluationResponse;
import com.banking.carddelivery.domain.dto.EnderecoDTO;
import com.banking.carddelivery.domain.entity.RegiaoAtendimento;
import com.banking.carddelivery.domain.enums.CardType;

public interface DeliveryEvaluator {

    boolean aplicaPara(CardType cardType, RegiaoAtendimento regiao);

    DeliveryEvaluationResponse avaliar(String cep, CardType cardType, EnderecoDTO endereco, RegiaoAtendimento regiao);
}
