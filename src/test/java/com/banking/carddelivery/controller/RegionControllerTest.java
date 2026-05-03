package com.banking.carddelivery.controller;

import com.banking.carddelivery.domain.dto.RegionDTO;
import com.banking.carddelivery.domain.entity.RegiaoAtendimento;
import com.banking.carddelivery.domain.enums.PerfilRisco;
import com.banking.carddelivery.repository.RegiaoAtendimentoRepository;
import com.banking.carddelivery.service.audit.AuditWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegionController.class)
class RegionControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private RegiaoAtendimentoRepository repository;
    @MockBean private AuditWriter auditWriter;

    @Test
    void listAll_returns200WithPage() throws Exception {
        when(repository.findAllByAtivoTrue(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(buildRegiaoEntity())));

        mockMvc.perform(get("/api/regions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].uf").value("SP"))
                .andExpect(jsonPath("$.content[0].perfilRisco").value("BAIXO"));
    }

    @Test
    void listAll_emptyPage_returns200() throws Exception {
        when(repository.findAllByAtivoTrue(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/regions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void save_validDto_returns200() throws Exception {
        RegionDTO dto = buildRegionDto();
        when(repository.save(any())).thenReturn(buildRegiaoEntity());

        mockMvc.perform(post("/api/regions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uf").value("SP"))
                .andExpect(jsonPath("$.perfilRisco").value("BAIXO"));
    }

    @Test
    void save_missingRequiredFields_returns400() throws Exception {
        mockMvc.perform(post("/api/regions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void save_nullPermiteEntregaAndAtivo_defaultsToTrue() throws Exception {
        RegionDTO dto = buildRegionDto();
        dto.setPermiteEntrega(null);
        dto.setAtivo(null);
        when(repository.save(any())).thenReturn(buildRegiaoEntity());

        mockMvc.perform(post("/api/regions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    private RegiaoAtendimento buildRegiaoEntity() {
        return RegiaoAtendimento.builder()
                .id(1L).uf("SP").cepInicio("01000000").cepFim("01999999")
                .perfilRisco("BAIXO").prazoDias(7).permiteEntrega(true).ativo(true).build();
    }

    private RegionDTO buildRegionDto() {
        return RegionDTO.builder()
                .uf("SP").cepInicio("01000000").cepFim("01999999")
                .perfilRisco(PerfilRisco.BAIXO).prazoDias(7)
                .permiteEntrega(true).ativo(true).build();
    }
}
