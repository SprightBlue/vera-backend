package com.unlam.verabackend.application.service;

import com.unlam.verabackend.infrastructure.entity.AlertEntity;
import com.unlam.verabackend.infrastructure.repository.AlertJpaRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AlertService {

    private final AlertJpaRepository alertJpaRepository;

    public AlertService(AlertJpaRepository alertJpaRepository) {
        this.alertJpaRepository = alertJpaRepository;
    }

    public List<AlertEntity> getAllAlerts() {
        return alertJpaRepository.findAll();
    }
}
