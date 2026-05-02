package com.banking.carddelivery.service.audit.impl;

import com.banking.carddelivery.domain.dto.AuditDTO;
import com.banking.carddelivery.domain.entity.AuditoriaConsulta;
import com.banking.carddelivery.repository.AuditoriaConsultaRepository;
import com.banking.carddelivery.service.audit.AuditReader;
import com.banking.carddelivery.service.audit.AuditWriter;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditServiceImpl implements AuditWriter, AuditReader {

    private static final Logger log = LoggerFactory.getLogger(AuditServiceImpl.class);

    private final AuditoriaConsultaRepository repository;

    public AuditServiceImpl(AuditoriaConsultaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void salvar(AuditoriaConsulta auditoria) {
        try {
            repository.save(auditoria);
            log.info("Auditoria persistida para eventId={}", auditoria.getEventId());
        } catch (DataIntegrityViolationException ex) {
            // eventId UNIQUE — evento duplicado descartado silenciosamente (idempotência)
            log.debug("Evento duplicado ignorado para eventId={}", auditoria.getEventId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditDTO> findAll(String customerId, LocalDateTime dataInicio, LocalDateTime dataFim, Pageable pageable) {
        return repository.findByFilters(customerId, dataInicio, dataFim, pageable)
                .map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditDTO> findByCep(String cep) {
        return repository.findByCepConsultado(cep).stream()
                .map(this::toDTO)
                .toList();
    }

    private AuditDTO toDTO(AuditoriaConsulta a) {
        return AuditDTO.builder()
                .id(a.getId())
                .eventId(a.getEventId())
                .cepConsultado(a.getCepConsultado())
                .customerId(a.getCustomerId())
                .cardType(a.getCardType())
                .dataHora(a.getDataHora())
                .decisao(a.getDecisao())
                .modalidade(a.getModalidade())
                .prazoDias(a.getPrazoDias())
                .riscoRegiao(a.getRiscoRegiao())
                .status(a.getStatus())
                .tempoRespostaMs(a.getTempoRespostaMs())
                .build();
    }
}

