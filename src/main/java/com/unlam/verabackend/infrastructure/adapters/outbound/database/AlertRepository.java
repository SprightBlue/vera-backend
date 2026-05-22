package com.unlam.verabackend.infrastructure.adapters.outbound.database;

import org.springframework.data.jpa.repository.JpaRepository;
import com.unlam.verabackend.domain.entities.Alert;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    
}
