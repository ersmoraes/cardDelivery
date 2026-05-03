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
class HighRiskRegionEvaluatorTest {

    @Mock private BranchService branchService;
    @InjectMocks private HighRiskRegionEvaluator evaluator;

    @Test
    void aplicaPara_altoRisk_returnsTrue() {
        assertThat(evaluator.aplicaPara(CardType.DEBIT, buildRegiao("ALTO", true))).isTrue();
    }

    @Test
    void aplicaPara_entregaFalse_returnsTrue() {
        assertThat(evaluator.aplicaPara(CardType.BLACK, buildRegiao("BAIXO", false))).isTrue();
    }

    @Test
    void aplicaPara_altoRiskAndEntregaFalse_returnsTrue() {
        assertThat(evaluator.aplicaPara(CardType.CREDIT, buildRegiao("ALTO", false))).isTrue();
    }

    @Test
    void aplicaPara_baixoRiskWithEntrega_returnsFalse() {
        assertThat(evaluator.aplicaPara(CardType.DEBIT, buildRegiao("BAIXO", true))).isFalse();
    }

    @Test
    void aplicaPara_medioRiskWithEntrega_returnsFalse() {
        assertThat(evaluator.aplicaPara(CardType.CREDIT, buildRegiao("MEDIO", true))).isFalse();
    }

    @Test
    void avaliar_returnsRetiradaAgencia() {
        RegiaoAtendimento regiao = buildRegiao("ALTO", false);
        when(branchService.findByUf("SP")).thenReturn(List.of(
                BranchDTO.builder().codigo("AGE001").build(),
                BranchDTO.builder().codigo("AGE002").build(),
                BranchDTO.builder().codigo("AGE003").build(),
                BranchDTO.builder().codigo("AGE004").build()));

        DeliveryEvaluationResponse result = evaluator.avaliar(
                "01310100", CardType.DEBIT, EnderecoDTO.builder().uf("SP").build(), regiao);

        assertThat(result.getDecisao()).isEqualTo(DeliveryDecision.RETIRADA_AGENCIA);
        assertThat(result.getAgenciasSugeridas()).hasSize(3);
        assertThat(result.getAgenciasSugeridas()).contains("AGE001", "AGE002", "AGE003");
    }

    @Test
    void avaliar_fewerThan3Branches_returnsAllAvailable() {
        RegiaoAtendimento regiao = buildRegiao("ALTO", false);
        when(branchService.findByUf("SP")).thenReturn(List.of(
                BranchDTO.builder().codigo("AGE001").build()));

        DeliveryEvaluationResponse result = evaluator.avaliar(
                "01310100", CardType.DEBIT, EnderecoDTO.builder().uf("SP").build(), regiao);

        assertThat(result.getAgenciasSugeridas()).hasSize(1);
    }

    private RegiaoAtendimento buildRegiao(String risco, boolean permiteEntrega) {
        return RegiaoAtendimento.builder()
                .uf("SP").perfilRisco(risco).permiteEntrega(permiteEntrega)
                .prazoDias(5).ativo(true).build();
    }
}
