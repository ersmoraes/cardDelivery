package com.banking.carddelivery.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "auditoria_consulta")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class AuditoriaConsulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 36)
    private String eventId;

    @Column(name = "cep_consultado", nullable = false, length = 9)
    private String cepConsultado;

    @Column(name = "customer_id", nullable = false, length = 20)
    private String customerId;

    @Column(name = "card_type", nullable = false, length = 20)
    private String cardType;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "resposta_api", columnDefinition = "jsonb")
    private String respostaApi;

    @Column(name = "decisao", nullable = false, length = 30)
    private String decisao;

    @Column(name = "modalidade", length = 30)
    private String modalidade;

    @Column(name = "prazo_dias")
    private Integer prazoDias;

    @Column(name = "risco_regiao", length = 10)
    private String riscoRegiao;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "tempo_resposta_ms")
    private Long tempoRespostaMs;

}
