package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.application.service.AlertService;
import com.unlam.verabackend.presentation.dto.AlertResponseDTO;

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
        Long caregiverId = 2L;

        AlertResponseDTO alert = new AlertResponseDTO(
                "550e8400-e29b-41d4-a716-446655440000", 
                "Alerta Detectada", 
                "Email solicitando datos", 
                "MEDIUM", 
                "WHATSAPP", 
                LocalDateTime.now().toString()
        );
        when(alertService.getAlertsByCaregiver(caregiverId)).thenReturn(List.of(alert));

        ResponseEntity<List<AlertResponseDTO>> response = alertController.getAlerts(caregiverId);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Alerta Detectada", response.getBody().get(0).title());
    }
}