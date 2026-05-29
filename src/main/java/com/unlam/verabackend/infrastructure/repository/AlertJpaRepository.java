package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.infrastructure.entity.AlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertJpaRepository extends JpaRepository<AlertEntity, Long> {
}