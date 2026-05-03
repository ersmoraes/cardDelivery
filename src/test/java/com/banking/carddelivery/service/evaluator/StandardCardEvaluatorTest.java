package com.banking.carddelivery.service.evaluator;

import com.banking.carddelivery.domain.dto.DeliveryEvaluationResponse;
import com.banking.carddelivery.domain.dto.EnderecoDTO;
import com.banking.carddelivery.domain.entity.RegiaoAtendimento;
import com.banking.carddelivery.domain.enums.CardType;
import com.banking.carddelivery.domain.enums.DeliveryDecision;
import com.banking.carddelivery.domain.enums.Modalidade;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StandardCardEvaluatorTest {

    private final StandardCardEvaluator evaluator = new StandardCardEvaluator();

    @Test
    void aplicaPara_debitCard_entregaTrue_returnsTrue() {
        assertThat(evaluator.aplicaPara(CardType.DEBIT, buildRegiao("BAIXO", true))).isTrue();
    }

    @Test
    void aplicaPara_creditCard_entregaTrue_returnsTrue() {
        assertThat(evaluator.aplicaPara(CardType.CREDIT, buildRegiao("BAIXO", true))).isTrue();
    }

    @Test
    void aplicaPara_multipleCard_entregaTrue_returnsTrue() {
        assertThat(evaluator.aplicaPara(CardType.MULTIPLE, buildRegiao("BAIXO", true))).isTrue();
    }

    @Test
    void aplicaPara_blackCard_returnsFalse() {
        assertThat(evaluator.aplicaPara(CardType.BLACK, buildRegiao("BAIXO", true))).isFalse();
    }

    @Test
    void aplicaPara_entregaFalse_returnsFalse() {
        assertThat(evaluator.aplicaPara(CardType.DEBIT, buildRegiao("BAIXO", false))).isFalse();
    }

    @Test
    void avaliar_baixoRisco_usesCorreios() {
        RegiaoAtendimento regiao = buildRegiao("BAIXO", true);
        regiao.setPrazoDias(7);

        DeliveryEvaluationResponse result = evaluator.avaliar(
                "01310100", CardType.DEBIT, EnderecoDTO.builder().uf("SP").build(), regiao);

        assertThat(result.getDecisao()).isEqualTo(DeliveryDecision.APROVADO);
        assertThat(result.getModalidade()).isEqualTo(Modalidade.CORREIOS);
        assertThat(result.getExigeAssinatura()).isFalse();
        assertThat(result.getRastreavel()).isTrue();
        assertThat(result.getPrazoDiasUteis()).isEqualTo(7);
    }

    @Test
    void avaliar_medioRisco_usesTransportadora() {
        RegiaoAtendimento regiao = buildRegiao("MEDIO", true);
        regiao.setTransportadora("CORREIOS_EXPRESSO");

        DeliveryEvaluationResponse result = evaluator.avaliar(
                "01310100", CardType.CREDIT, EnderecoDTO.builder().uf("SP").build(), regiao);

        assertThat(result.getModalidade()).isEqualTo(Modalidade.TRANSPORTADORA_SEGURA);
        assertThat(result.getExigeAssinatura()).isTrue();
    }

    @Test
    void avaliar_cepAndEnderecoPassedThrough() {
        RegiaoAtendimento regiao = buildRegiao("BAIXO", true);
        EnderecoDTO endereco = EnderecoDTO.builder().uf("SP").cidade("São Paulo").build();

        DeliveryEvaluationResponse result = evaluator.avaliar("01310100", CardType.DEBIT, endereco, regiao);

        assertThat(result.getCep()).isEqualTo("01310100");
        assertThat(result.getEndereco()).isEqualTo(endereco);
    }

    private RegiaoAtendimento buildRegiao(String risco, boolean permiteEntrega) {
        return RegiaoAtendimento.builder()
                .uf("SP").perfilRisco(risco).permiteEntrega(permiteEntrega)
                .prazoDias(5).ativo(true).build();
    }
}
