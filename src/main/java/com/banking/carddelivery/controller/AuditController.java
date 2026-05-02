package com.banking.carddelivery.controller;

import com.banking.carddelivery.domain.dto.AuditDTO;
import com.banking.carddelivery.service.audit.AuditReader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditReader auditReader;

    public AuditController(AuditReader auditReader) {
        this.auditReader = auditReader;
    }

    @GetMapping("/queries")
    public ResponseEntity<Page<AuditDTO>> queries(
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            Pageable pageable) {
        return ResponseEntity.ok(auditReader.findAll(customerId, dataInicio, dataFim, pageable));
    }

    @GetMapping("/queries/{cep}")
    public ResponseEntity<List<AuditDTO>> queriesByCep(@PathVariable String cep) {
        return ResponseEntity.ok(auditReader.findByCep(cep));
    }
}

