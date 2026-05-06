package com.banking.carddelivery.controller;

import com.banking.carddelivery.domain.dto.AuditDTO;
import com.banking.carddelivery.service.audit.AuditReader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditController.class)
class AuditControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private AuditReader auditReader;

    @Test
    void queries_noFilters_returns200() throws Exception {
        AuditDTO dto = AuditDTO.builder().id(1L).eventId("test-id").cepConsultado("01310100").build();
        when(auditReader.findAll(isNull(), isNull(), isNull(), any()))
                .thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/audit/queries"))
                .andExpect(status().isOk());
    }

    @Test
    void queries_emptyResult_returns200() throws Exception {
        when(auditReader.findAll(isNull(), isNull(), isNull(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/audit/queries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void queriesByCep_validCep_returns200() throws Exception {
        AuditDTO dto = AuditDTO.builder().id(1L).cepConsultado("01310100").decisao("APROVADO").build();
        when(auditReader.findByCep("01310100")).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/audit/queries/01310100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cepConsultado").value("01310100"))
                .andExpect(jsonPath("$[0].decisao").value("APROVADO"));
    }

    @Test
    void queriesByCep_noResults_returns200WithEmptyList() throws Exception {
        when(auditReader.findByCep("99999999")).thenReturn(List.of());

        mockMvc.perform(get("/api/audit/queries/99999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void queries_withCustomerId_returns200() throws Exception {
        when(auditReader.findAll(any(), isNull(), isNull(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/audit/queries").param("customerId", "C123456"))
                .andExpect(status().isOk());
    }

    @Test
    void queries_invalidDataInicio_returns400WithTimestamp() throws Exception {
        mockMvc.perform(get("/api/audit/queries").param("dataInicio", "not-a-date"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.title").value("Tipo de Parâmetro Inválido"));
    }
}
