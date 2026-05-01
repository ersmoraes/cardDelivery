package com.banking.carddelivery.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "agencia")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Agencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "uf", nullable = false, length = 2)
    private String uf;

    @Column(name = "cidade", nullable = false, length = 100)
    private String cidade;

    @Column(name = "cep", nullable = false, length = 8)
    private String cep;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo;
}
