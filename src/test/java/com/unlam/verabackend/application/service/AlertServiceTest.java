package com.unlam.verabackend.application.service;

import com.unlam.verabackend.infrastructure.entity.RiskAlertEntity;
import com.unlam.verabackend.infrastructure.repository.RiskAlertJpaRepository;
import com.unlam.verabackend.presentation.dto.AlertResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private RiskAlertJpaRepository riskAlertJpaRepository;

    @InjectMocks
    private AlertService alertService;

    @Test
    void getAllAlerts_ShouldReturnListOfAlertResponseDTO() {
        
        RiskAlertEntity mockAlert = mock(RiskAlertEntity.class, Answers.RETURNS_DEEP_STUBS);
        
        UUID fakeId = UUID.randomUUID();
        when(mockAlert.getId()).thenReturn(fakeId);
        when(mockAlert.getAnalysis().getRiskLevelId()).thenReturn("HIGH");
        when(mockAlert.getAnalysis().getContentSourceId()).thenReturn("WHATSAPP");
        when(mockAlert.getAnalysis().getSuspiciousPatterns()).thenReturn("Enlace de phishing detectado");
        when(mockAlert.getCreatedAt()).thenReturn(LocalDateTime.now());

        when(riskAlertJpaRepository.findAll()).thenReturn(List.of(mockAlert));

        
        List<AlertResponseDTO> result = alertService.getAllAlerts();

        
        assertEquals(1, result.size());
        assertEquals("HIGH", result.get(0).riskLevel());
        assertEquals("WHATSAPP", result.get(0).source());
        assertEquals("Enlace de phishing detectado", result.get(0).description());
    }
}