package com.banking.carddelivery.service.audit;

import com.banking.carddelivery.domain.entity.AuditoriaConsulta;

public interface AuditWriter {

    void salvar(AuditoriaConsulta auditoria);
}
