package com.banking.carddelivery.controller;

import com.banking.carddelivery.domain.dto.RegionDTO;
import com.banking.carddelivery.domain.entity.RegiaoAtendimento;
import com.banking.carddelivery.domain.enums.PerfilRisco;
import com.banking.carddelivery.repository.RegiaoAtendimentoRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/regions")
public class RegionController {

    private final RegiaoAtendimentoRepository repository;

    public RegionController(RegiaoAtendimentoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<Page<RegionDTO>> listAll(Pageable pageable) {
        return ResponseEntity.ok(repository.findAllByAtivoTrue(pageable).map(this::toDTO));
    }

    @PostMapping
    public ResponseEntity<RegionDTO> save(@Valid @RequestBody RegionDTO dto) {
        RegiaoAtendimento entity = toEntity(dto);
        RegiaoAtendimento saved = repository.save(entity);
        return ResponseEntity.ok(toDTO(saved));
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
                .permiteEntrega(dto.getPermiteEntrega() != null ? dto.getPermiteEntrega() : true)
                .ativo(dto.getAtivo() != null ? dto.getAtivo() : true)
                .build();
    }
}

