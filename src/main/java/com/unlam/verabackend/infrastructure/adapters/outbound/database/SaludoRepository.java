package com.unlam.verabackend.infrastructure.adapters.outbound.database;

import com.unlam.verabackend.domain.entities.Saludo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaludoRepository extends JpaRepository<Saludo, Long> {
}