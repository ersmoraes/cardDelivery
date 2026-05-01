package com.banking.carddelivery.repository;

import com.banking.carddelivery.domain.entity.Agencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgenciaRepository extends JpaRepository<Agencia, Long> {

    List<Agencia> findByUfAndAtivoTrueOrderByCodigo(String uf);
    List<Agencia> findByCepStartingWithAndAtivoTrue(String cepPrefix);
}
