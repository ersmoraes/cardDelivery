package com.banking.carddelivery.service;

import com.banking.carddelivery.domain.dto.BranchDTO;
import com.banking.carddelivery.domain.entity.Agencia;
import com.banking.carddelivery.repository.AgenciaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BranchService {

    private final AgenciaRepository agenciaRepository;

    public BranchService(AgenciaRepository agenciaRepository) {
        this.agenciaRepository = agenciaRepository;
    }

    public List<BranchDTO> findByUf(String uf) {
        return agenciaRepository.findByUfAndAtivoTrueOrderByCodigo(uf).stream()
                .map(this::toDTO)
                .toList();
    }

    public List<BranchDTO> findNearbyCep(String cep) {
        String prefix = cep.length() >= 5 ? cep.substring(0, 5) : cep;
        return agenciaRepository.findByCepStartingWithAndAtivoTrue(prefix).stream()
                .map(this::toDTO)
                .toList();
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
