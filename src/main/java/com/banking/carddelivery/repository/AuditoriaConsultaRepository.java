package com.banking.carddelivery.repository;

import com.banking.carddelivery.domain.entity.AuditoriaConsulta;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AuditoriaConsultaRepository extends JpaRepository<AuditoriaConsulta, Long> {

    List<AuditoriaConsulta> findByCepConsultado(String cepConsultado);
    Optional<AuditoriaConsulta> findByEventId(String eventId);

    boolean existsByEventId(String eventId);

    @Query(value = """
            SELECT * FROM auditoria_consulta
            WHERE (CAST(:customerId AS varchar) IS NULL OR customer_id = :customerId)
            AND (CAST(:dataInicio AS timestamp) IS NULL OR data_hora >= CAST(:dataInicio AS timestamp))
            AND (CAST(:dataFim AS timestamp) IS NULL OR data_hora <= CAST(:dataFim AS timestamp))
            ORDER BY data_hora DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM auditoria_consulta
            WHERE (CAST(:customerId AS varchar) IS NULL OR customer_id = :customerId)
            AND (CAST(:dataInicio AS timestamp) IS NULL OR data_hora >= CAST(:dataInicio AS timestamp))
            AND (CAST(:dataFim AS timestamp) IS NULL OR data_hora <= CAST(:dataFim AS timestamp))
            """,
            nativeQuery = true)
    Page<AuditoriaConsulta> findByFilters(
            @Param("customerId") String customerId,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            Pageable pageable);
}
