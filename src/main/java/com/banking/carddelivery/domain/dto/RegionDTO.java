package com.banking.carddelivery.domain.dto;

import com.banking.carddelivery.domain.enums.PerfilRisco;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegionDTO {
    private Long id;

    @NotBlank(message = "UF é obrigatória")
    private String uf;

    @NotBlank(message = "CEP início é obrigatório")
    private String cepInicio;

    @NotBlank(message = "CEP fim é obrigatório")
    private String cepFim;

    @NotNull(message = "Perfil de risco é obrigatório")
    private PerfilRisco perfilRisco;

    @NotNull(message = "Prazo em dias é obrigatório")
    @Positive(message = "Prazo deve ser positivo")
    private Integer prazoDias;

    private String transportadora;
    private Boolean permiteEntrega;
    private Boolean ativo;
}

