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

    @Query("""
            SELECT a FROM AuditoriaConsulta a
                        WHERE (:customerId IS NULL OR a.customerId = :customerId)
                                   AND (:dataInicio IS NULL OR a.dataHora >= :dataInicio)
                                   AND (:dataFim    IS NULL OR a.dataHora <= :dataFim)
                        ORDER BY a.dataHora DESC
            """)
    Page<AuditoriaConsulta> findByFilters(
            @Param("customerId") String customerId,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            Pageable pageable);
}
