package com.banking.carddelivery.service.audit;

import com.banking.carddelivery.domain.entity.AuditoriaConsulta;
import com.banking.carddelivery.repository.AuditoriaConsultaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class JpaAuditWriter implements AuditWriter {

    private static final Logger log = LoggerFactory.getLogger(JpaAuditWriter.class);

    private final AuditoriaConsultaRepository repository;

    public JpaAuditWriter(AuditoriaConsultaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void salvar(AuditoriaConsulta auditoria) {
        if (repository.existsByEventId(auditoria.getEventId())) {
            log.warn("Auditoria já registrada para eventId={}, ignorando duplicata", auditoria.getEventId());
            return;
        }
        repository.save(auditoria);
        log.debug("Auditoria persistida para eventId={}", auditoria.getEventId());
    }
}
