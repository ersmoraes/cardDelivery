package com.banking.carddelivery.controller;

import com.banking.carddelivery.domain.dto.RegionDTO;
import com.banking.carddelivery.domain.entity.AuditoriaConsulta;
import com.banking.carddelivery.domain.entity.RegiaoAtendimento;
import com.banking.carddelivery.domain.enums.PerfilRisco;
import com.banking.carddelivery.repository.RegiaoAtendimentoRepository;
import com.banking.carddelivery.service.audit.AuditWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/regions")
public class RegionController {

    private static final Logger log = LoggerFactory.getLogger(RegionController.class);

    private final RegiaoAtendimentoRepository repository;
    private final AuditWriter auditWriter;
    private final ObjectMapper objectMapper;

    public RegionController(RegiaoAtendimentoRepository repository,
                            AuditWriter auditWriter,
                            ObjectMapper objectMapper) {
        this.repository = repository;
        this.auditWriter = auditWriter;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<Page<RegionDTO>> listAll(Pageable pageable) {
        long inicio = System.currentTimeMillis();
        Page<RegionDTO> resultado = repository.findAllByAtivoTrue(pageable).map(this::toDTO);
        registrarAuditoria("N/A", resultado.getContent(), "CONSULTA_REGIOES",
                System.currentTimeMillis() - inicio);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping
    public ResponseEntity<RegionDTO> save(@Valid @RequestBody RegionDTO dto) {
        long inicio = System.currentTimeMillis();
        RegiaoAtendimento entity = toEntity(dto);
        RegiaoAtendimento saved = repository.save(entity);
        RegionDTO resposta = toDTO(saved);
        registrarAuditoria("N/A", resposta, "CADASTRO_REGIAO",
                System.currentTimeMillis() - inicio);
        return ResponseEntity.ok(resposta);
    }

    private void registrarAuditoria(String cep, Object resposta, String decisao, long tempoMs) {
        try {
            AuditoriaConsulta auditoria = AuditoriaConsulta.builder()
                    .eventId(UUID.randomUUID().toString())
                    .cepConsultado(cep)
                    .dataHora(LocalDateTime.now())
                    .respostaApi(objectMapper.writeValueAsString(resposta))
                    .decisao(decisao)
                    .status("SUCESSO")
                    .tempoRespostaMs(tempoMs)
                    .build();
            auditWriter.salvar(auditoria);
        } catch (Exception ex) {
            log.warn("Falha ao registrar auditoria: {}", ex.getMessage());
        }
    }

    private RegionDTO toDTO(RegiaoAtendimento r) {
        return RegionDTO.builder()
                .id(r.getId())
                .uf(r.getUf())
                .cepInicio(r.getCepInicio())
                .cepFim(r.getCepFim())
                .perfilRisco(PerfilRisco.valueOf(r.getPerfilRisco()))
                .prazoDias(r.getPrazoDias())
                .transportadora(r.getTransportadora())
                .permiteEntrega(r.getPermiteEntrega())
                .ativo(r.getAtivo())
                .build();
    }

    private RegiaoAtendimento toEntity(RegionDTO dto) {
        return RegiaoAtendimento.builder()
                .id(dto.getId())
                .uf(dto.getUf())
                .cepInicio(dto.getCepInicio())
                .cepFim(dto.getCepFim())
                .perfilRisco(dto.getPerfilRisco().name())
                .prazoDias(dto.getPrazoDias())
                .transportadora(dto.getTransportadora())
                .permiteEntrega(!Boolean.FALSE.equals(dto.getPermiteEntrega()))
                .ativo(!Boolean.FALSE.equals(dto.getAtivo()))
                .build();
    }
}
