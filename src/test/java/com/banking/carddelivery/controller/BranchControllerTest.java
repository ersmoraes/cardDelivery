package com.banking.carddelivery.controller;

import com.banking.carddelivery.domain.dto.BranchDTO;
import com.banking.carddelivery.service.BranchService;
import com.banking.carddelivery.util.CepNormalizer;
import com.banking.carddelivery.util.CepValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BranchController.class)
class BranchControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private BranchService branchService;
    @MockBean private CepValidator cepValidator;
    @MockBean private CepNormalizer cepNormalizer;

    @Test
    void nearby_validCep_returns200WithBranches() throws Exception {
        String cep = "01310100";
        when(cepValidator.isValid(cep)).thenReturn(true);
        when(cepNormalizer.normalize(cep)).thenReturn(cep);
        when(branchService.findNearbyCep(cep)).thenReturn(List.of(
                BranchDTO.builder().codigo("AGE001").nome("Agência SP Centro")
                        .uf("SP").cidade("São Paulo").cep("01310100").build()));

        mockMvc.perform(get("/api/cards/delivery/branches/nearby").param("cep", cep))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("AGE001"))
                .andExpect(jsonPath("$[0].uf").value("SP"));
    }

    @Test
    void nearby_invalidCep_returns400() throws Exception {
        String cep = "invalid";
        when(cepValidator.isValid(cep)).thenReturn(false);

        mockMvc.perform(get("/api/cards/delivery/branches/nearby").param("cep", cep))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nearby_validCepWithDash_normalizes() throws Exception {
        String raw = "01310-100";
        String normalized = "01310100";
        when(cepValidator.isValid(raw)).thenReturn(true);
        when(cepNormalizer.normalize(raw)).thenReturn(normalized);
        when(branchService.findNearbyCep(normalized)).thenReturn(List.of());

        mockMvc.perform(get("/api/cards/delivery/branches/nearby").param("cep", raw))
                .andExpect(status().isOk());
    }

    @Test
    void nearby_emptyResult_returns200WithEmptyList() throws Exception {
        String cep = "99999999";
        when(cepValidator.isValid(cep)).thenReturn(true);
        when(cepNormalizer.normalize(cep)).thenReturn(cep);
        when(branchService.findNearbyCep(cep)).thenReturn(List.of());

        mockMvc.perform(get("/api/cards/delivery/branches/nearby").param("cep", cep))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
