package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.model.DashboardData;
import com.unlam.verabackend.domain.model.Role;
import com.unlam.verabackend.domain.port.out.AlertsRepository;
import com.unlam.verabackend.domain.port.out.AnalysisRepository;
import com.unlam.verabackend.domain.port.out.UserLocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para GetDashboardDataUseCaseImpl")
class GetDashboardDataUseCaseImplTest {

    @Mock private AnalysisRepository analysisRepository;
    @Mock private AlertsRepository alertsRepository;
    @Mock private UserLocationRepository userLocationRepository;

    @InjectMocks
    private GetDashboardDataUseCaseImpl getDashboardDataUseCase;

    private String userEmail;

    @BeforeEach
    void setUp() {
        userEmail = "test@unlam.edu.ar";
    }

    @Test
    @DisplayName("Debería retornar métricas de PROTECTED cuando el rol es PROTECTED")
    void execute_ProtectedRole_ShouldPopulateProtectedMetrics() {
        // Arrange
        when(analysisRepository.findTop3ByUserEmail(userEmail)).thenReturn(Collections.emptyList());
        when(analysisRepository.countByUserEmailInLast24Hours(userEmail)).thenReturn(5L);
        when(alertsRepository.findTop3ResolvedAlertsByUserEmail(userEmail)).thenReturn(Collections.emptyList());
        when(alertsRepository.countResolvedAlertsInLast24Hours(userEmail)).thenReturn(2L);

        // Act
        DashboardData result = getDashboardDataUseCase.execute(userEmail, Role.PROTECTED);

        // Assert
        assertNotNull(result);
        assertEquals(5L, result.getAnalysisInLast24Hours());
        assertEquals(2L, result.getResolvedAlertsInLast24Hours());

        verify(alertsRepository, never()).findTop3ActiveAlertsByCarerEmail(anyString());
        verify(userLocationRepository, never()).findTop3LastConnectedByCarerEmail(anyString());
    }

    @Test
    @DisplayName("Debería retornar métricas de CARER cuando el rol es CARER")
    void execute_CarerRole_ShouldPopulateCarerMetrics() {
        // Arrange
        when(alertsRepository.findTop3ActiveAlertsByCarerEmail(userEmail)).thenReturn(Collections.emptyList());
        when(alertsRepository.countAlertsByCarerEmailInLast24Hours(userEmail)).thenReturn(10L);
        when(userLocationRepository.findTop3LastConnectedByCarerEmail(userEmail)).thenReturn(Collections.emptyList());
        when(userLocationRepository.countConnectedUsersByCarerEmail(userEmail)).thenReturn(3L);

        // Act
        DashboardData result = getDashboardDataUseCase.execute(userEmail, Role.CARER);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getAlertsInLast24Hours());
        assertEquals(3L, result.getConnectedUsersCount());

        verify(analysisRepository, never()).findTop3ByUserEmail(anyString());
        verify(alertsRepository, never()).findTop3ResolvedAlertsByUserEmail(anyString());
    }

    @Test
    @DisplayName("Debería retornar un objeto sin métricas si el rol es desconocido")
    void execute_UnknownRole_ShouldReturnEmptyDashboard() {
        // Act
        DashboardData result = getDashboardDataUseCase.execute(userEmail, Role.ADMIN);

        // Assert
        assertNotNull(result, "El objeto DashboardData no debería ser nulo");

        assertEquals(0L, result.getAnalysisInLast24Hours(), "Las métricas deberían ser 0 si no se asignaron datos");
        assertEquals(0L, result.getAlertsInLast24Hours(), "Las métricas deberían ser 0 si no se asignaron datos");
        assertEquals(0L, result.getConnectedUsersCount(), "Las métricas deberían ser 0 si no se asignaron datos");
        assertEquals(0L, result.getResolvedAlertsInLast24Hours(), "Las métricas deberían ser 0 si no se asignaron datos");

        assertTrue(result.getTop3Analysis() == null || result.getTop3Analysis().isEmpty());
        assertTrue(result.getTop3Alerts() == null || result.getTop3Alerts().isEmpty());
        assertTrue(result.getTop3ConnectedUsers() == null || result.getTop3ConnectedUsers().isEmpty());

        verifyNoInteractions(analysisRepository);
        verifyNoInteractions(alertsRepository);
        verifyNoInteractions(userLocationRepository);
    }
}