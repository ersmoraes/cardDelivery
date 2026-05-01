package com.banking.carddelivery.repository;

import com.banking.carddelivery.domain.entity.RegiaoAtendimento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RegiaoAtendimentoRepository extends JpaRepository<RegiaoAtendimento, Long> {

    @Query("""
            SELECT r FROM RegiaoAtendimento r
            WHERE r.ativo = TRUE
              AND :cep BETWEEN r.cepInicio AND r.cepFim
            ORDER BY r.cepInicio ASC
            """)
    Optional<RegiaoAtendimento> findByCep(@Param("cep") String cep);

    Page<RegiaoAtendimento> findAllByAtivoTrue(Pageable pageable);
}
