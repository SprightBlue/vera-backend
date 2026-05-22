package com.unlam.verabackend.application.services;

import com.unlam.verabackend.domain.entities.Alert;
import com.unlam.verabackend.infrastructure.adapters.outbound.database.AlertRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    
    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }
    
    public List<Alert> getAllAlerts() {
        return alertRepository.findAll();
    }
}
