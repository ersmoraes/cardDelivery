package com.banking.carddelivery.service.impl;

import com.banking.carddelivery.domain.dto.DeliveryEvaluationRequest;
import com.banking.carddelivery.domain.dto.DeliveryEvaluationResponse;
import com.banking.carddelivery.domain.dto.EnderecoDTO;
import com.banking.carddelivery.domain.entity.RegiaoAtendimento;
import com.banking.carddelivery.domain.enums.CardType;
import com.banking.carddelivery.domain.enums.DeliveryDecision;
import com.banking.carddelivery.messaging.EventPublisher;
import com.banking.carddelivery.provider.CepProvider;
import com.banking.carddelivery.repository.RegiaoAtendimentoRepository;
import com.banking.carddelivery.service.evaluator.DeliveryEvaluator;
import com.banking.carddelivery.util.CepNormalizer;
import com.banking.carddelivery.util.CustomerIdMasker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryEvaluationServiceImplTest {

    @Mock private CepProvider cepProvider;
    @Mock private DeliveryEvaluator evaluator;
    @Mock private EventPublisher eventPublisher;
    @Mock private RegiaoAtendimentoRepository regiaoRepository;
    @Mock private CepNormalizer cepNormalizer;
    @Mock private CustomerIdMasker customerIdMasker;

    private DeliveryEvaluationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DeliveryEvaluationServiceImpl(
                cepProvider, List.of(evaluator), eventPublisher,
                regiaoRepository, cepNormalizer, customerIdMasker);
    }

    @Test
    void evaluate_success_returnsResponse() {
        String rawCep = "01310-100";
        String normalizedCep = "01310100";
        DeliveryEvaluationRequest request = buildRequest(rawCep);
        EnderecoDTO endereco = EnderecoDTO.builder().uf("SP").cidade("São Paulo").build();
        RegiaoAtendimento regiao = buildRegiao("BAIXO", true);
        DeliveryEvaluationResponse expected = DeliveryEvaluationResponse.builder()
                .cep(normalizedCep).decisao(DeliveryDecision.APROVADO).build();

        when(cepNormalizer.normalize(rawCep)).thenReturn(normalizedCep);
        when(customerIdMasker.mask(any())).thenReturn("C12***");
        when(cepProvider.buscarEndereco(normalizedCep)).thenReturn(endereco);
        when(regiaoRepository.findByCep(normalizedCep)).thenReturn(Optional.of(regiao));
        when(evaluator.aplicaPara(CardType.DEBIT, regiao)).thenReturn(true);
        when(evaluator.avaliar(eq(normalizedCep), eq(CardType.DEBIT), eq(endereco), eq(regiao))).thenReturn(expected);

        DeliveryEvaluationResponse result = service.evaluate(request);

        assertThat(result.getDecisao()).isEqualTo(DeliveryDecision.APROVADO);
    }

    @Test
    void evaluate_passesNormalizedCepToEvaluator() {
        String rawCep = "01310-100";
        String normalizedCep = "01310100";
        DeliveryEvaluationRequest request = buildRequest(rawCep);

        when(cepNormalizer.normalize(rawCep)).thenReturn(normalizedCep);
        when(customerIdMasker.mask(any())).thenReturn("C12***");
        when(cepProvider.buscarEndereco(normalizedCep)).thenReturn(EnderecoDTO.builder().uf("SP").build());
        when(regiaoRepository.findByCep(normalizedCep)).thenReturn(Optional.of(buildRegiao("BAIXO", true)));
        when(evaluator.aplicaPara(any(), any())).thenReturn(true);
        when(evaluator.avaliar(any(), any(), any(), any())).thenReturn(
                DeliveryEvaluationResponse.builder().decisao(DeliveryDecision.APROVADO).build());

        service.evaluate(request);

        verify(evaluator).avaliar(eq(normalizedCep), any(), any(), any());
        verify(evaluator, never()).avaliar(eq(rawCep), any(), any(), any());
    }

    @Test
    void evaluate_usesFallbackRegiao_whenNotFound() {
        String cep = "01310100";
        DeliveryEvaluationRequest request = buildRequest(cep);

        when(cepNormalizer.normalize(cep)).thenReturn(cep);
        when(customerIdMasker.mask(any())).thenReturn("C12***");
        when(cepProvider.buscarEndereco(cep)).thenReturn(EnderecoDTO.builder().uf("SP").build());
        when(regiaoRepository.findByCep(cep)).thenReturn(Optional.empty());
        when(evaluator.aplicaPara(any(), any())).thenReturn(true);
        when(evaluator.avaliar(any(), any(), any(), any())).thenReturn(
                DeliveryEvaluationResponse.builder().decisao(DeliveryDecision.RETIRADA_AGENCIA).build());

        DeliveryEvaluationResponse result = service.evaluate(request);

        assertThat(result).isNotNull();
        verify(evaluator).aplicaPara(eq(CardType.DEBIT),
                argThat(r -> "ALTO".equals(r.getPerfilRisco()) && Boolean.FALSE.equals(r.getPermiteEntrega())));
    }

    @Test
    void evaluate_ignoresEventPublishFailure() {
        String cep = "01310100";
        DeliveryEvaluationRequest request = buildRequest(cep);

        when(cepNormalizer.normalize(cep)).thenReturn(cep);
        when(customerIdMasker.mask(any())).thenReturn("C12***");
        when(cepProvider.buscarEndereco(cep)).thenReturn(EnderecoDTO.builder().uf("SP").build());
        when(regiaoRepository.findByCep(cep)).thenReturn(Optional.of(buildRegiao("BAIXO", true)));
        when(evaluator.aplicaPara(any(), any())).thenReturn(true);
        when(evaluator.avaliar(any(), any(), any(), any())).thenReturn(
                DeliveryEvaluationResponse.builder().decisao(DeliveryDecision.APROVADO).build());
        doThrow(new RuntimeException("RabbitMQ down")).when(eventPublisher).publicar(any());

        assertThatNoException().isThrownBy(() -> service.evaluate(request));
    }

    @Test
    void evaluate_throwsIllegalState_whenNoEvaluatorApplies() {
        String cep = "01310100";
        DeliveryEvaluationRequest request = buildRequest(cep);

        when(cepNormalizer.normalize(cep)).thenReturn(cep);
        when(customerIdMasker.mask(any())).thenReturn("C12***");
        when(cepProvider.buscarEndereco(cep)).thenReturn(EnderecoDTO.builder().uf("SP").build());
        when(regiaoRepository.findByCep(cep)).thenReturn(Optional.of(buildRegiao("BAIXO", true)));
        when(evaluator.aplicaPara(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> service.evaluate(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Nenhum evaluator");
    }

    private DeliveryEvaluationRequest buildRequest(String cep) {
        return DeliveryEvaluationRequest.builder()
                .cep(cep).cardType(CardType.DEBIT).customerId("C123456").build();
    }

    private RegiaoAtendimento buildRegiao(String risco, boolean permiteEntrega) {
        return RegiaoAtendimento.builder()
                .uf("SP").cepInicio("01000000").cepFim("01999999")
                .perfilRisco(risco).prazoDias(5).permiteEntrega(permiteEntrega).ativo(true).build();
    }
}
