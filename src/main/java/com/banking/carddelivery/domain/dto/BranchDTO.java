package com.banking.carddelivery.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchDTO {
    private String codigo;
    private String nome;
    private String uf;
    private String cidade;
    private String cep;
}
