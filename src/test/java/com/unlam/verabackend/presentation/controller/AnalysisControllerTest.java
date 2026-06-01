package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.model.*;
import com.unlam.verabackend.domain.ports.in.AnalyzeMessageUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnalysisControllerTest {

    @Mock
    private AnalyzeMessageUseCase analyzeMessageUseCase;

    @InjectMocks
    private AnalysisController analysisController;

    @Test
    void shouldReturnCreatedStatusAndAnalysisResponse() {
        AnalysisController.AnalysisRequest request = new AnalysisController.AnalysisRequest(
                1L,
                "juan@mail.com",
                "Hola, necesito tu clave bancaria",
                "WHATSAPP"
        );

        DomainUser mockUser = new DomainUser(1L, "Juan Pérez", "juan@mail.com", Role.ROLE_USER, LocalDateTime.now(), LocalDateTime.now(), true, true);
        UUID expectedId = UUID.randomUUID();

        Analysis mockAnalysis = new Analysis(
                expectedId,
                mockUser,
                request.content(),
                MessageSource.WHATSAPP,
                RiskLevel.HIGH,
                "Patrón sospechoso",
                "Ojo, no respondas",
                LocalDateTime.now()
        );

        when(analyzeMessageUseCase.analyzeMessage(any(DomainUser.class), eq(request.content()), eq(MessageSource.WHATSAPP)))
                .thenReturn(mockAnalysis);

        ResponseEntity<AnalysisController.AnalysisResponse> responseEntity = analysisController.analyzeMessage(request);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode()); // Verifies HTTP 201

        AnalysisController.AnalysisResponse responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals(expectedId.toString(), responseBody.id());
        assertEquals("HIGH", responseBody.riskLevel());
        assertEquals("Patrón sospechoso", responseBody.suspiciousPatterns());
        assertEquals("Ojo, no respondas", responseBody.recommendation());

        verify(analyzeMessageUseCase, times(1))
                .analyzeMessage(any(DomainUser.class), eq(request.content()), eq(MessageSource.WHATSAPP));
    }
}