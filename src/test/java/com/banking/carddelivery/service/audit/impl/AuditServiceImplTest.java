package com.banking.carddelivery.service.audit.impl;

import com.banking.carddelivery.domain.dto.AuditDTO;
import com.banking.carddelivery.domain.entity.AuditoriaConsulta;
import com.banking.carddelivery.repository.AuditoriaConsultaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceImplTest {

    @Mock private AuditoriaConsultaRepository repository;
    @InjectMocks private AuditServiceImpl service;

    @Test
    void salvar_success_callsRepository() {
        AuditoriaConsulta auditoria = buildAuditoria();

        service.salvar(auditoria);

        verify(repository).save(auditoria);
    }

    @Test
    void salvar_duplicateEvent_ignoredSilently() {
        AuditoriaConsulta auditoria = buildAuditoria();
        doThrow(new DataIntegrityViolationException("unique constraint"))
                .when(repository).save(auditoria);

        assertThatNoException().isThrownBy(() -> service.salvar(auditoria));
    }

    @Test
    void findAll_returnsPagedDtos() {
        AuditoriaConsulta a = buildAuditoria();
        when(repository.findByFilters(null, null, null, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(a)));

        Page<AuditDTO> result = service.findAll(null, null, null, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEventId()).isEqualTo("test-event-id");
        assertThat(result.getContent().get(0).getCepConsultado()).isEqualTo("01310100");
    }

    @Test
    void findAll_withFilters_delegatesToRepository() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 12, 31, 23, 59);
        when(repository.findByFilters("C123456", start, end, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of()));

        Page<AuditDTO> result = service.findAll("C123456", start, end, Pageable.unpaged());

        assertThat(result.getContent()).isEmpty();
        verify(repository).findByFilters("C123456", start, end, Pageable.unpaged());
    }

    @Test
    void findByCep_returnsList() {
        when(repository.findByCepConsultado("01310100")).thenReturn(List.of(buildAuditoria()));

        List<AuditDTO> result = service.findByCep("01310100");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDecisao()).isEqualTo("APROVADO");
    }

    @Test
    void findByCep_empty_returnsEmptyList() {
        when(repository.findByCepConsultado("99999999")).thenReturn(List.of());

        assertThat(service.findByCep("99999999")).isEmpty();
    }

    private AuditoriaConsulta buildAuditoria() {
        return AuditoriaConsulta.builder()
                .id(1L).eventId("test-event-id").cepConsultado("01310100")
                .customerId("C123456").cardType("DEBIT")
                .dataHora(LocalDateTime.of(2024, 6, 15, 10, 30))
                .decisao("APROVADO").modalidade("CORREIOS").prazoDias(5)
                .riscoRegiao("BAIXO").status("SUCESSO").tempoRespostaMs(120L)
                .build();
    }
}
