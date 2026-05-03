package com.banking.carddelivery.service;

import com.banking.carddelivery.domain.dto.BranchDTO;
import com.banking.carddelivery.domain.entity.Agencia;
import com.banking.carddelivery.domain.entity.AuditoriaConsulta;
import com.banking.carddelivery.repository.AgenciaRepository;
import com.banking.carddelivery.service.audit.AuditWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BranchService {

    private static final Logger log = LoggerFactory.getLogger(BranchService.class);

    private final AgenciaRepository agenciaRepository;
    private final AuditWriter auditWriter;
    private final ObjectMapper objectMapper;

    public BranchService(AgenciaRepository agenciaRepository,
                         AuditWriter auditWriter,
                         ObjectMapper objectMapper) {
        this.agenciaRepository = agenciaRepository;
        this.auditWriter = auditWriter;
        this.objectMapper = objectMapper;
    }

    public List<BranchDTO> findByUf(String uf) {
        return agenciaRepository.findByUfAndAtivoTrueOrderByCodigo(uf).stream()
                .map(this::toDTO)
                .toList();
    }

    public List<BranchDTO> findNearbyCep(String cep) {
        long inicio = System.currentTimeMillis();
        String prefix = cep.length() >= 5 ? cep.substring(0, 5) : cep;

        List<BranchDTO> resultado = agenciaRepository.findByCepStartingWithAndAtivoTrue(prefix).stream()
                .map(this::toDTO)
                .toList();

        registrarAuditoria(cep, resultado, System.currentTimeMillis() - inicio);
        return resultado;
    }

    private void registrarAuditoria(String cep, List<BranchDTO> resultado, long tempoMs) {
        try {
            AuditoriaConsulta auditoria = AuditoriaConsulta.builder()
                    .eventId(UUID.randomUUID().toString())
                    .cepConsultado(cep)
                    .dataHora(LocalDateTime.now())
                    .respostaApi(objectMapper.writeValueAsString(resultado))
                    .decisao("CONSULTA_AGENCIA")
                    .status("SUCESSO")
                    .tempoRespostaMs(tempoMs)
                    .build();
            auditWriter.salvar(auditoria);
        } catch (Exception ex) {
            log.warn("Falha ao registrar auditoria para CEP={}: {}", cep, ex.getMessage());
        }
    }

    private BranchDTO toDTO(Agencia agencia) {
        return BranchDTO.builder()
                .codigo(agencia.getCodigo())
                .nome(agencia.getNome())
                .uf(agencia.getUf())
                .cidade(agencia.getCidade())
                .cep(agencia.getCep())
                .build();
    }
}
