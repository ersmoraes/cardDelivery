package com.banking.carddelivery.provider;

import com.banking.carddelivery.domain.dto.EnderecoDTO;
import com.banking.carddelivery.exception.CepNaoEncontradoException;
import com.banking.carddelivery.exception.ServicoExternoIndisponivelException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class ViaCepProvider implements CepProvider {

    private static final Logger log = LoggerFactory.getLogger(ViaCepProvider.class);

    private final WebClient webClient;

    public ViaCepProvider(WebClient.Builder webClientBuilder,
                          @Value("${cep.api.url}") String cepApiUrl) {
        this.webClient = webClientBuilder.baseUrl(cepApiUrl).build();
    }

    @Override
    @Cacheable(value = "cep", key = "#cep")
    @CircuitBreaker(name = "cepProvider", fallbackMethod = "fallback")
    @Retry(name = "cepProvider")
    public EnderecoDTO buscarEndereco(String cep) {
        log.info("Consultando CEP: {}", cep);

        ViaCepResponse response = webClient.get()
                .uri("/ws/{cep}/json/", cep)
                .retrieve()
                .bodyToMono(ViaCepResponse.class)
                .block();

        if (response == null || Boolean.TRUE.equals(response.getErro())) {
            throw new CepNaoEncontradoException("CEP não encontrado: " + cep);
        }

        return EnderecoDTO.builder()
                .logradouro(response.getLogradouro())
                .bairro(response.getBairro())
                .cidade(response.getLocalidade())
                .uf(response.getUf())
                .build();
    }

    @SuppressWarnings("unused")
    private EnderecoDTO fallback(String cep, WebClientResponseException ex) {
        log.warn("Serviço de CEP indisponível para CEP {}. Status: {}", cep, ex.getStatusCode());
        throw new ServicoExternoIndisponivelException("Serviço de CEP indisponível após tentativas: " + ex.getMessage());
    }

    @SuppressWarnings("unused")
    private EnderecoDTO fallback(String cep, Exception ex) {
        log.warn("Serviço de CEP indisponível para CEP {}. Causa: {}", cep, ex.getMessage());
        throw new ServicoExternoIndisponivelException("Serviço de CEP indisponível: " + ex.getMessage());
    }
}

