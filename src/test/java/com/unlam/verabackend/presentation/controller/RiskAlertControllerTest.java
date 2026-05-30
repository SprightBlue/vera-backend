package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.model.*;
import com.unlam.verabackend.domain.ports.inbound.ManageRiskAlertUseCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RiskAlertControllerTest {

    @Mock
    private ManageRiskAlertUseCase manageRiskAlertUseCase;

    @InjectMocks
    private RiskAlertController riskAlertController;

    private RiskAlert mockAlert;
    private String mockAlertId;

    @BeforeEach
    void setUp() {
        mockAlertId = UUID.randomUUID().toString();
        UUID mockAnalysisId = UUID.randomUUID();

        DomainUser mockElderly = new DomainUser(1L, "Abuelo Juan", "abuelo@mail.com", Role.ROLE_USER, LocalDateTime.now(), LocalDateTime.now(), true, true);
        DomainUser mockCaregiver = new DomainUser(2L, "Cuidador Pedro", "pedro@mail.com", Role.ROLE_ADMIN, LocalDateTime.now(), LocalDateTime.now(), true, true);

        Analysis mockAnalysis = new Analysis(mockAnalysisId, mockElderly, "Contenido sospechoso", MessageSource.TELEGRAM, RiskLevel.HIGH, "patron", "recom", LocalDateTime.now());

        mockAlert = new RiskAlert(UUID.fromString(mockAlertId), mockAnalysis, mockCaregiver, false, LocalDateTime.now());
    }

    @Test
    void shouldReturnActiveAlertsList() {
        Long caregiverId = 2L;
        when(manageRiskAlertUseCase.getActiveAlertsByCaregiver(caregiverId)).thenReturn(List.of(mockAlert));

        ResponseEntity<List<RiskAlertController.RiskAlertResponse>> responseEntity = riskAlertController.getActiveAlerts(caregiverId);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        List<RiskAlertController.RiskAlertResponse> body = responseEntity.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());

        assertEquals(mockAlertId, body.getFirst().alertId());
        assertEquals("Abuelo Juan", body.getFirst().protectedUserName());
        assertEquals("Contenido sospechoso", body.getFirst().messageContent());
        assertEquals("Telegram", body.getFirst().source());
        assertEquals("HIGH", body.getFirst().riskLevel());
        assertEquals("patron", body.getFirst().suspiciousPatterns());

        verify(manageRiskAlertUseCase, times(1)).getActiveAlertsByCaregiver(caregiverId);
    }

    @Test
    void shouldReturnNoContentWhenAlertIsMarkedAsSolved() {
        doNothing().when(manageRiskAlertUseCase).markAlertAsSolved(mockAlertId);

        ResponseEntity<Void> responseEntity = riskAlertController.solveAlert(mockAlertId);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());

        verify(manageRiskAlertUseCase, times(1)).markAlertAsSolved(mockAlertId);
    }

    @Test
    void shouldReturnContactMailtoLink() {
        String expectedMailto = "mailto:abuelo@mail.com?subject=Seguimiento";
        when(manageRiskAlertUseCase.getContactLinkForUser(mockAlertId)).thenReturn(expectedMailto);

        ResponseEntity<RiskAlertController.ContactLinkResponse> responseEntity = riskAlertController.getContactLink(mockAlertId);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        RiskAlertController.ContactLinkResponse body = responseEntity.getBody();
        assertNotNull(body);
        assertEquals(expectedMailto, body.link());

        verify(manageRiskAlertUseCase, times(1)).getContactLinkForUser(mockAlertId);
    }
}
