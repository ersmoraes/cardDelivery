package com.banking.carddelivery.controller;

import com.banking.carddelivery.domain.dto.DeliveryEvaluationRequest;
import com.banking.carddelivery.domain.dto.DeliveryEvaluationResponse;
import com.banking.carddelivery.domain.enums.CardType;
import com.banking.carddelivery.domain.enums.DeliveryDecision;
import com.banking.carddelivery.exception.CepInvalidoException;
import com.banking.carddelivery.exception.CepNaoEncontradoException;
import com.banking.carddelivery.exception.ServicoExternoIndisponivelException;
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
class DeliveryEvaluationControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private DeliveryEvaluationService deliveryEvaluationService;

    @Test
    void evaluate_validRequest_returns200() throws Exception {
        DeliveryEvaluationRequest request = DeliveryEvaluationRequest.builder()
                .cep("01310100").cardType(CardType.DEBIT).customerId("C123456").build();
        DeliveryEvaluationResponse response = DeliveryEvaluationResponse.builder()
                .cep("01310100").decisao(DeliveryDecision.APROVADO).build();
        when(deliveryEvaluationService.evaluate(any())).thenReturn(response);

        mockMvc.perform(post("/api/cards/delivery/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decisao").value("APROVADO"))
                .andExpect(jsonPath("$.cep").value("01310100"));
    }

    @Test
    void evaluate_emptyBody_returns400() throws Exception {
        mockMvc.perform(post("/api/cards/delivery/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void evaluate_invalidCepFormat_returns400() throws Exception {
        DeliveryEvaluationRequest request = DeliveryEvaluationRequest.builder()
                .cep("invalid-cep").cardType(CardType.DEBIT).customerId("C123456").build();

        mockMvc.perform(post("/api/cards/delivery/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void evaluate_invalidCustomerId_returns400() throws Exception {
        DeliveryEvaluationRequest request = DeliveryEvaluationRequest.builder()
                .cep("01310100").cardType(CardType.DEBIT).customerId("invalid").build();

        mockMvc.perform(post("/api/cards/delivery/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void evaluate_cepNotFound_returns404() throws Exception {
        DeliveryEvaluationRequest request = DeliveryEvaluationRequest.builder()
                .cep("99999999").cardType(CardType.DEBIT).customerId("C123456").build();
        when(deliveryEvaluationService.evaluate(any()))
                .thenThrow(new CepNaoEncontradoException("CEP não encontrado"));

        mockMvc.perform(post("/api/cards/delivery/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void evaluate_serviceUnavailable_returns503() throws Exception {
        DeliveryEvaluationRequest request = DeliveryEvaluationRequest.builder()
                .cep("01310100").cardType(CardType.DEBIT).customerId("C123456").build();
        when(deliveryEvaluationService.evaluate(any()))
                .thenThrow(new ServicoExternoIndisponivelException("Serviço indisponível"));

        mockMvc.perform(post("/api/cards/delivery/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void evaluate_invalidCep_returns400() throws Exception {
        DeliveryEvaluationRequest request = DeliveryEvaluationRequest.builder()
                .cep("01310100").cardType(CardType.DEBIT).customerId("C123456").build();
        when(deliveryEvaluationService.evaluate(any()))
                .thenThrow(new CepInvalidoException("CEP inválido"));

        mockMvc.perform(post("/api/cards/delivery/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void evaluate_unexpectedError_returns500() throws Exception {
        DeliveryEvaluationRequest request = DeliveryEvaluationRequest.builder()
                .cep("01310100").cardType(CardType.DEBIT).customerId("C123456").build();
        when(deliveryEvaluationService.evaluate(any()))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/api/cards/delivery/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }
}
