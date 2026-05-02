package com.banking.carddelivery.service.impl;

import com.banking.carddelivery.domain.dto.DeliveryEvaluationRequest;
import com.banking.carddelivery.domain.dto.DeliveryEvaluationResponse;
import com.banking.carddelivery.domain.dto.EnderecoDTO;
import com.banking.carddelivery.domain.entity.RegiaoAtendimento;
import com.banking.carddelivery.domain.enums.DeliveryDecision;
import com.banking.carddelivery.domain.enums.PerfilRisco;
import com.banking.carddelivery.messaging.EventPublisher;
import com.banking.carddelivery.messaging.event.DeliveryEvaluatedEvent;
import com.banking.carddelivery.provider.CepProvider;
import com.banking.carddelivery.repository.RegiaoAtendimentoRepository;
import com.banking.carddelivery.service.DeliveryEvaluationService;
import com.banking.carddelivery.service.evaluator.DeliveryEvaluator;
import com.banking.carddelivery.util.CepNormalizer;
import com.banking.carddelivery.util.CustomerIdMasker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DeliveryEvaluationServiceImpl implements DeliveryEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryEvaluationServiceImpl.class);

    private final CepProvider cepProvider;
    private final List<DeliveryEvaluator> evaluators;
    private final EventPublisher eventPublisher;
    private final RegiaoAtendimentoRepository regiaoRepository;
    private final CepNormalizer cepNormalizer;
    private final CustomerIdMasker customerIdMasker;

    public DeliveryEvaluationServiceImpl(CepProvider cepProvider,
                                         List<DeliveryEvaluator> evaluators,
                                         EventPublisher eventPublisher,
                                         RegiaoAtendimentoRepository regiaoRepository,
                                         CepNormalizer cepNormalizer,
                                         CustomerIdMasker customerIdMasker) {
        this.cepProvider = cepProvider;
        this.evaluators = evaluators;
        this.eventPublisher = eventPublisher;
        this.regiaoRepository = regiaoRepository;
        this.cepNormalizer = cepNormalizer;
        this.customerIdMasker = customerIdMasker;
    }

    @Override
    public DeliveryEvaluationResponse evaluate(DeliveryEvaluationRequest request) {
        long inicio = System.currentTimeMillis();
        String cepNormalizado = cepNormalizer.normalize(request.getCep());
        String maskedCustomerId = customerIdMasker.mask(request.getCustomerId());

        log.info("Avaliando entrega para CEP={} cardType={} customerId={}",
                cepNormalizado, request.getCardType(), maskedCustomerId);

        EnderecoDTO endereco = cepProvider.buscarEndereco(cepNormalizado);

        RegiaoAtendimento regiao = regiaoRepository.findByCep(cepNormalizado)
                .orElseGet(() -> buildDefaultRegiao(cepNormalizado));

        DeliveryEvaluationResponse response = evaluators.stream()
                .filter(e -> e.aplicaPara(request.getCardType(), regiao))
                .findFirst()
                .map(e -> e.avaliar(request.getCep(), request.getCardType(), endereco, regiao))
                .orElseThrow(() -> new IllegalStateException("Nenhum evaluator aplicável encontrado"));

        long tempoMs = System.currentTimeMillis() - inicio;

        publishEvent(request, response, cepNormalizado, tempoMs, "SUCESSO");

        log.info("Avaliação concluída em {}ms para customerId={} decisão={}",
                tempoMs, maskedCustomerId, response.getDecisao());

        return response;
    }

    private void publishEvent(DeliveryEvaluationRequest request,
                              DeliveryEvaluationResponse response,
                              String cepNormalizado,
                              long tempoMs,
                              String status) {
        try {
            DeliveryEvaluatedEvent event = DeliveryEvaluatedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .cep(cepNormalizado)
                    .customerId(request.getCustomerId())
                    .cardType(request.getCardType())
                    .ocorridoEm(LocalDateTime.now())
                    .endereco(response.getEndereco())
                    .decisao(response.getDecisao() != null ? response.getDecisao() : DeliveryDecision.NEGADO)
                    .modalidade(response.getModalidade())
                    .prazoDiasUteis(response.getPrazoDiasUteis())
                    .riscoRegiao(response.getRiscoRegiao() != null ? response.getRiscoRegiao() : PerfilRisco.BAIXO)
                    .tempoRespostaMs(tempoMs)
                    .status(status)
                    .build();

            eventPublisher.publicar(event);
        } catch (Exception ex) {
            log.warn("Falha ao publicar evento de auditoria — resposta não foi afetada: {}", ex.getMessage());
        }
    }

    private RegiaoAtendimento buildDefaultRegiao(String cep) {
        log.warn("Região não cadastrada para CEP {}. Usando fallback com ALTO risco.", cep);
        return RegiaoAtendimento.builder()
                .uf("XX")
                .cepInicio(cep)
                .cepFim(cep)
                .perfilRisco(PerfilRisco.ALTO.name())
                .prazoDias(0)
                .permiteEntrega(false)
                .ativo(true)
                .build();
    }
}
