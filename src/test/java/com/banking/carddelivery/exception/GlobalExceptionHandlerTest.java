package com.banking.carddelivery.exception;

import com.banking.carddelivery.controller.DeliveryEvaluationController;
import com.banking.carddelivery.domain.dto.DeliveryEvaluationRequest;
import com.banking.carddelivery.domain.enums.CardType;
import com.banking.carddelivery.service.DeliveryEvaluationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeliveryEvaluationController.class)
class GlobalExceptionHandlerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private DeliveryEvaluationService deliveryEvaluationService;

    private static final String ENDPOINT = "/api/cards/delivery/evaluate";

    @Test
    void handleCepInvalido_returns400WithTimestamp() throws Exception {
        when(deliveryEvaluationService.evaluate(any()))
                .thenThrow(new CepInvalidoException("CEP inválido"));

        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(validBody()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.title").value("CEP Inválido"));
    }

    @Test
    void handleCepNaoEncontrado_returns404WithTimestamp() throws Exception {
        when(deliveryEvaluationService.evaluate(any()))
                .thenThrow(new CepNaoEncontradoException("CEP não encontrado"));

        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(validBody()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.title").value("CEP Não Encontrado"));
    }

    @Test
    void handleCardTypeInvalido_returns400WithTimestamp() throws Exception {
        when(deliveryEvaluationService.evaluate(any()))
                .thenThrow(new CardTypeInvalidoException("Tipo inválido"));

        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(validBody()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.title").value("Tipo de Cartão Inválido"));
    }

    @Test
    void handleServicoIndisponivel_returns503WithTimestamp() throws Exception {
        when(deliveryEvaluationService.evaluate(any()))
                .thenThrow(new ServicoExternoIndisponivelException("S3 indisponível"));

        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(validBody()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.title").value("Serviço Externo Indisponível"));
    }

    @Test
    void handleValidation_returns400WithTimestampAndCampos() throws Exception {
        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.campos").exists())
                .andExpect(jsonPath("$.title").value("Erro de Validação"));
    }

    @Test
    void handleGeneric_returns500WithTimestamp() throws Exception {
        when(deliveryEvaluationService.evaluate(any()))
                .thenThrow(new RuntimeException("Erro inesperado"));

        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(validBody()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.title").value("Erro Interno"));
    }

    private String validBody() throws Exception {
        return objectMapper.writeValueAsString(
                DeliveryEvaluationRequest.builder()
                        .cep("01310100").cardType(CardType.DEBIT).customerId("C123456").build());
    }
}
