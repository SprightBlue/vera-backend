package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.application.service.AlertService;
import com.unlam.verabackend.domain.model.RiskLevel;
import com.unlam.verabackend.infrastructure.entity.AlertEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertControllerTest {

    @Mock
    private AlertService alertService;

    @InjectMocks
    private AlertController alertController;

    @Test
    void getAlerts_ShouldReturn200AndAlertList() {
        
        AlertEntity alert = new AlertEntity(
                1L, 
                "Solicitud Sospechosa", 
                "Email solicitando datos personales", 
                RiskLevel.MEDIUM, 
                "Email", 
                LocalDateTime.now()
        );
        when(alertService.getAllAlerts()).thenReturn(List.of(alert));

        
        ResponseEntity<List<AlertEntity>> response = alertController.getAlerts();

       
        assertEquals(200, response.getStatusCode().value(), "El código de estado debe ser 200 OK");
        assertEquals(1, response.getBody().size(), "Debe devolver exactamente 1 alerta");
        assertEquals("Solicitud Sospechosa", response.getBody().get(0).getTitle(), "El título debe coincidir");
        assertEquals(RiskLevel.MEDIUM, response.getBody().get(0).getRiskLevel(), "El nivel de riesgo debe coincidir");
    }
}