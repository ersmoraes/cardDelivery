package com.banking.carddelivery.controller;

import com.banking.carddelivery.domain.dto.BranchDTO;
import com.banking.carddelivery.exception.CepInvalidoException;
import com.banking.carddelivery.service.BranchService;
import com.banking.carddelivery.util.CepNormalizer;
import com.banking.carddelivery.util.CepValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cards/delivery/branches")
public class BranchController {

    private final BranchService branchService;
    private final CepValidator cepValidator;
    private final CepNormalizer cepNormalizer;

    public BranchController(BranchService branchService,
                            CepValidator cepValidator,
                            CepNormalizer cepNormalizer) {
        this.branchService = branchService;
        this.cepValidator = cepValidator;
        this.cepNormalizer = cepNormalizer;
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<BranchDTO>> nearby(@RequestParam String cep) {
        if (!cepValidator.isValid(cep)) {
            throw new CepInvalidoException("CEP inválido: " + cep);
        }
        String cepNorm = cepNormalizer.normalize(cep);
        return ResponseEntity.ok(branchService.findNearbyCep(cepNorm));
    }
}
