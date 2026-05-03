package com.banking.carddelivery.service;

import com.banking.carddelivery.domain.dto.BranchDTO;
import com.banking.carddelivery.domain.entity.Agencia;
import com.banking.carddelivery.repository.AgenciaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchServiceTest {

    @Mock private AgenciaRepository agenciaRepository;
    @InjectMocks private BranchService service;

    @Test
    void findByUf_returnsDto() {
        when(agenciaRepository.findByUfAndAtivoTrueOrderByCodigo("SP"))
                .thenReturn(List.of(buildAgencia("AGE001", "SP")));

        List<BranchDTO> result = service.findByUf("SP");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCodigo()).isEqualTo("AGE001");
        assertThat(result.get(0).getUf()).isEqualTo("SP");
    }

    @Test
    void findByUf_emptyResult_returnsEmpty() {
        when(agenciaRepository.findByUfAndAtivoTrueOrderByCodigo("ZZ")).thenReturn(List.of());

        assertThat(service.findByUf("ZZ")).isEmpty();
    }

    @Test
    void findNearbyCep_normalCep_usesFirst5Digits() {
        when(agenciaRepository.findByCepStartingWithAndAtivoTrue("01310"))
                .thenReturn(List.of(buildAgencia("AGE001", "SP")));

        List<BranchDTO> result = service.findNearbyCep("01310100");

        assertThat(result).hasSize(1);
    }

    @Test
    void findNearbyCep_shortCep_usesFullCep() {
        when(agenciaRepository.findByCepStartingWithAndAtivoTrue("0131"))
                .thenReturn(List.of(buildAgencia("AGE001", "SP")));

        List<BranchDTO> result = service.findNearbyCep("0131");

        assertThat(result).hasSize(1);
    }

    @Test
    void findNearbyCep_dtoFieldsMappedCorrectly() {
        when(agenciaRepository.findByCepStartingWithAndAtivoTrue("01310"))
                .thenReturn(List.of(buildAgencia("AGE002", "SP")));

        List<BranchDTO> result = service.findNearbyCep("01310100");

        BranchDTO dto = result.get(0);
        assertThat(dto.getCodigo()).isEqualTo("AGE002");
        assertThat(dto.getNome()).isEqualTo("Agência Teste");
        assertThat(dto.getCidade()).isEqualTo("São Paulo");
        assertThat(dto.getCep()).isEqualTo("01310100");
    }

    private Agencia buildAgencia(String codigo, String uf) {
        return Agencia.builder()
                .codigo(codigo).nome("Agência Teste").uf(uf)
                .cidade("São Paulo").cep("01310100").ativo(true).build();
    }
}
