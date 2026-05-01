package com.banking.carddelivery.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "regiao_atendimento")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class RegiaoAtendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uf", nullable = false, length = 2)
    private String uf;

    @Column(name = "cep_inicio", nullable = false, length = 8)
    private String cepInicio;

    @Column(name = "cep_fim", nullable = false, length = 8)
    private String cepFim;

    @Column(name = "perfil_risco", nullable = false, length = 10)
    private String perfilRisco;

    @Column(name = "prazo_dias", nullable = false)
    private Integer prazoDias;

    @Column(name = "transportadora", length = 50)
    private String transportadora;

    @Column(name = "permite_entrega", nullable = false)
    private Boolean permiteEntrega;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo;
}

