package com.banking.carddelivery.service.evaluator;

import com.banking.carddelivery.domain.dto.BranchDTO;
import com.banking.carddelivery.domain.dto.DeliveryEvaluationResponse;
import com.banking.carddelivery.domain.dto.EnderecoDTO;
import com.banking.carddelivery.domain.entity.RegiaoAtendimento;
import com.banking.carddelivery.domain.enums.CardType;
import com.banking.carddelivery.domain.enums.DeliveryDecision;
import com.banking.carddelivery.service.BranchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FallbackBranchPickupEvaluatorTest {

    @Mock private BranchService branchService;
    @InjectMocks private FallbackBranchPickupEvaluator evaluator;

    @Test
    void aplicaPara_alwaysTrue_anyCardType() {
        RegiaoAtendimento regiao = buildRegiao();
        assertThat(evaluator.aplicaPara(CardType.DEBIT, regiao)).isTrue();
        assertThat(evaluator.aplicaPara(CardType.BLACK, regiao)).isTrue();
        assertThat(evaluator.aplicaPara(CardType.CREDIT, regiao)).isTrue();
        assertThat(evaluator.aplicaPara(CardType.MULTIPLE, regiao)).isTrue();
    }

    @Test
    void avaliar_returnsRetiradaAgencia_withAgencias() {
        RegiaoAtendimento regiao = buildRegiao();
        when(branchService.findByUf("SP")).thenReturn(List.of(
                BranchDTO.builder().codigo("AGE001").build(),
                BranchDTO.builder().codigo("AGE002").build()));

        DeliveryEvaluationResponse result = evaluator.avaliar(
                "01310100", CardType.DEBIT, EnderecoDTO.builder().uf("SP").build(), regiao);

        assertThat(result.getDecisao()).isEqualTo(DeliveryDecision.RETIRADA_AGENCIA);
        assertThat(result.getAgenciasSugeridas()).contains("AGE001", "AGE002");
        assertThat(result.getMotivo()).isNotBlank();
    }

    @Test
    void avaliar_limitsTo3Branches() {
        RegiaoAtendimento regiao = buildRegiao();
        when(branchService.findByUf("SP")).thenReturn(List.of(
                BranchDTO.builder().codigo("AGE001").build(),
                BranchDTO.builder().codigo("AGE002").build(),
                BranchDTO.builder().codigo("AGE003").build(),
                BranchDTO.builder().codigo("AGE004").build()));

        DeliveryEvaluationResponse result = evaluator.avaliar(
                "01310100", CardType.DEBIT, EnderecoDTO.builder().uf("SP").build(), regiao);

        assertThat(result.getAgenciasSugeridas()).hasSize(3);
    }

    @Test
    void avaliar_noBranches_returnsEmptyList() {
        RegiaoAtendimento regiao = buildRegiao();
        when(branchService.findByUf("SP")).thenReturn(List.of());

        DeliveryEvaluationResponse result = evaluator.avaliar(
                "01310100", CardType.DEBIT, EnderecoDTO.builder().uf("SP").build(), regiao);

        assertThat(result.getAgenciasSugeridas()).isEmpty();
    }

    private RegiaoAtendimento buildRegiao() {
        return RegiaoAtendimento.builder()
                .uf("SP").perfilRisco("BAIXO").permiteEntrega(true)
                .prazoDias(5).ativo(true).build();
    }
}
