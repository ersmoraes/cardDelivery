package com.banking.carddelivery.service.evaluator;

import com.banking.carddelivery.domain.dto.BranchDTO;
import com.banking.carddelivery.domain.dto.DeliveryEvaluationResponse;
import com.banking.carddelivery.domain.dto.EnderecoDTO;
import com.banking.carddelivery.domain.entity.RegiaoAtendimento;
import com.banking.carddelivery.domain.enums.CardType;
import com.banking.carddelivery.domain.enums.DeliveryDecision;
import com.banking.carddelivery.domain.enums.PerfilRisco;
import com.banking.carddelivery.service.BranchService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Order(99)
public class FallbackBranchPickupEvaluator implements DeliveryEvaluator {

    private final BranchService branchService;

    public FallbackBranchPickupEvaluator(BranchService branchService) {
        this.branchService = branchService;
    }

    @Override
    public boolean aplicaPara(CardType cardType, RegiaoAtendimento regiao) {
        return true;
    }

    @Override
    public DeliveryEvaluationResponse avaliar(String cep, CardType cardType, EnderecoDTO endereco, RegiaoAtendimento regiao) {
        List<String> agencias = branchService.findByUf(regiao.getUf()).stream()
                .limit(3)
                .map(BranchDTO::getCodigo)
                .toList();

        return DeliveryEvaluationResponse.builder()
                .cep(cep)
                .endereco(endereco)
                .decisao(DeliveryDecision.RETIRADA_AGENCIA)
                .motivo("Não foi possível determinar entrega — utilize retirada em agência")
                .agenciasSugeridas(agencias)
                .riscoRegiao(PerfilRisco.valueOf(regiao.getPerfilRisco()))
                .consultadoEm(LocalDateTime.now())
                .build();
    }
}

