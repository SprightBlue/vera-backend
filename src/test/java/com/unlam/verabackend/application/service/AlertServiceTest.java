package com.unlam.verabackend.application.service;

import com.unlam.verabackend.domain.model.RiskLevel;
import com.unlam.verabackend.infrastructure.entity.AlertEntity;
import com.unlam.verabackend.infrastructure.repository.AlertJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertJpaRepository alertJpaRepository;

    @InjectMocks
    private AlertService alertService;

    @Test
    void getAllAlerts_ShouldReturnListOfAlerts() {
        AlertEntity alert = new AlertEntity(
                1L, 
                "Intento de Phishing", 
                "Mensaje sospechoso de Banco", 
                RiskLevel.HIGH, 
                "SMS", 
                LocalDateTime.now()
        );
        when(alertJpaRepository.findAll()).thenReturn(List.of(alert));

        List<AlertEntity> result = alertService.getAllAlerts();

        assertEquals(1, result.size());
        assertEquals("Intento de Phishing", result.get(0).getTitle());
        assertEquals(RiskLevel.HIGH, result.get(0).getRiskLevel());
    }
}
