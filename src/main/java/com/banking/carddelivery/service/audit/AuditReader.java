package com.banking.carddelivery.service.audit;

import com.banking.carddelivery.domain.dto.AuditDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditReader {

    Page<AuditDTO> findAll(String customerId, LocalDateTime dataInicio, LocalDateTime dataFim, Pageable pageable);

    List<AuditDTO> findByCep(String cep);
}
