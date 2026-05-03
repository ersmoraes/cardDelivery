package com.banking.carddelivery.service.evaluator;

import com.banking.carddelivery.domain.dto.DeliveryEvaluationResponse;
import com.banking.carddelivery.domain.dto.EnderecoDTO;
import com.banking.carddelivery.domain.entity.RegiaoAtendimento;
import com.banking.carddelivery.domain.enums.CardType;
import com.banking.carddelivery.domain.enums.DeliveryDecision;
import com.banking.carddelivery.domain.enums.Modalidade;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BlackCardExpressEvaluatorTest {

    private final BlackCardExpressEvaluator evaluator = new BlackCardExpressEvaluator();

    @Test
    void aplicaPara_blackCard_baixoRisco_entregaTrue_returnsTrue() {
        assertThat(evaluator.aplicaPara(CardType.BLACK, buildRegiao("BAIXO", true))).isTrue();
    }

    @Test
    void aplicaPara_blackCard_medioRisco_entregaTrue_returnsTrue() {
        assertThat(evaluator.aplicaPara(CardType.BLACK, buildRegiao("MEDIO", true))).isTrue();
    }

    @Test
    void aplicaPara_nonBlackCard_returnsFalse() {
        assertThat(evaluator.aplicaPara(CardType.DEBIT, buildRegiao("BAIXO", true))).isFalse();
        assertThat(evaluator.aplicaPara(CardType.CREDIT, buildRegiao("BAIXO", true))).isFalse();
        assertThat(evaluator.aplicaPara(CardType.MULTIPLE, buildRegiao("BAIXO", true))).isFalse();
    }

    @Test
    void aplicaPara_altoRisco_returnsFalse() {
        assertThat(evaluator.aplicaPara(CardType.BLACK, buildRegiao("ALTO", true))).isFalse();
    }

    @Test
    void aplicaPara_entregaFalse_returnsFalse() {
        assertThat(evaluator.aplicaPara(CardType.BLACK, buildRegiao("BAIXO", false))).isFalse();
    }

    @Test
    void avaliar_returnsAprovadoWithExpressDelivery() {
        RegiaoAtendimento regiao = buildRegiao("BAIXO", true);
        regiao.setTransportadora("FEDEX");

        DeliveryEvaluationResponse result = evaluator.avaliar(
                "01310100", CardType.BLACK, EnderecoDTO.builder().uf("SP").build(), regiao);

        assertThat(result.getDecisao()).isEqualTo(DeliveryDecision.APROVADO);
        assertThat(result.getModalidade()).isEqualTo(Modalidade.TRANSPORTADORA_SEGURA);
        assertThat(result.getPrazoDiasUteis()).isEqualTo(2);
        assertThat(result.getExigeAssinatura()).isTrue();
        assertThat(result.getRastreavel()).isTrue();
        assertThat(result.getTransportadora()).isEqualTo("FEDEX");
    }

    @Test
    void avaliar_nullTransportadora_usesDefaultExpressCard() {
        RegiaoAtendimento regiao = buildRegiao("BAIXO", true);
        regiao.setTransportadora(null);

        DeliveryEvaluationResponse result = evaluator.avaliar(
                "01310100", CardType.BLACK, EnderecoDTO.builder().uf("SP").build(), regiao);

        assertThat(result.getTransportadora()).isEqualTo("EXPRESS_CARD");
    }

    @Test
    void avaliar_cepPassedThrough() {
        RegiaoAtendimento regiao = buildRegiao("BAIXO", true);

        DeliveryEvaluationResponse result = evaluator.avaliar(
                "01310100", CardType.BLACK, EnderecoDTO.builder().uf("SP").build(), regiao);

        assertThat(result.getCep()).isEqualTo("01310100");
    }

    private RegiaoAtendimento buildRegiao(String risco, boolean permiteEntrega) {
        return RegiaoAtendimento.builder()
                .uf("SP").perfilRisco(risco).permiteEntrega(permiteEntrega)
                .prazoDias(5).ativo(true).build();
    }
}
