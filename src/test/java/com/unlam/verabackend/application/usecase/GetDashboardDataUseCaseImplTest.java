package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.model.DashboardData;
import com.unlam.verabackend.domain.model.Role;
import com.unlam.verabackend.domain.port.out.AlertsRepository;
import com.unlam.verabackend.domain.port.out.AnalysisRepository;
import com.unlam.verabackend.domain.port.out.ChatsRepository;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para GetDashboardDataUseCaseImpl")
class GetDashboardDataUseCaseImplTest {

    @Mock private AnalysisRepository analysisRepository;
    @Mock private AlertsRepository alertsRepository;
    @Mock private ChatsRepository chatsRepository;
    @Mock private TrustContactRepository trustContactRepository;

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
        TrustContact mockContact = new TrustContact();

        when(analysisRepository.countByUserEmailSince(eq(userEmail), any(LocalDateTime.class))).thenReturn(5L);
        when(alertsRepository.countAlertsByEmailSince(eq(userEmail), any(LocalDateTime.class))).thenReturn(3L);
        when(alertsRepository.countResolvedAlertsByEmailSince(eq(userEmail), any(LocalDateTime.class))).thenReturn(2L);
        when(chatsRepository.findLastUpdatedByUserEmail(userEmail)).thenReturn(Optional.empty());
        when(trustContactRepository.findFirstByProtectedUser_EmailOrderByCreatedAtDesc(userEmail)).thenReturn(Optional.of(mockContact));
        when(analysisRepository.findTop3ByUserEmail(userEmail)).thenReturn(Collections.emptyList());

        // Act
        DashboardData result = getDashboardDataUseCase.execute(userEmail, Role.PROTECTED);

        // Assert
        assertNotNull(result);
        assertEquals(5L, result.getAnalysisCountSince());
        assertEquals(3L, result.getAlertsCountSince());
        assertEquals(2L, result.getResolvedAlertsCountSince());
        assertNotNull(result.getLatestTrustContact());

        verify(alertsRepository, never()).findTop3ActiveAlertsByCarerEmail(anyString());
        verify(trustContactRepository, never()).findFirstByCarer_EmailOrderByCreatedAtDesc(anyString());
    }

    @Test
    @DisplayName("Debería retornar métricas de CARER cuando el rol es CARER")
    void execute_CarerRole_ShouldPopulateCarerMetrics() {
        // Arrange
        TrustContact mockContact = new TrustContact();

        when(analysisRepository.countByUserEmailSince(eq(userEmail), any(LocalDateTime.class))).thenReturn(10L);
        when(alertsRepository.countAlertsByEmailSince(eq(userEmail), any(LocalDateTime.class))).thenReturn(4L);
        when(alertsRepository.countResolvedAlertsByEmailSince(eq(userEmail), any(LocalDateTime.class))).thenReturn(1L);
        when(chatsRepository.findLastUpdatedByUserEmail(userEmail)).thenReturn(Optional.empty());
        when(trustContactRepository.findFirstByCarer_EmailOrderByCreatedAtDesc(userEmail)).thenReturn(Optional.of(mockContact));
        when(alertsRepository.findTop3ActiveAlertsByCarerEmail(userEmail)).thenReturn(Collections.emptyList());

        // Act
        DashboardData result = getDashboardDataUseCase.execute(userEmail, Role.CARER);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getAnalysisCountSince());
        assertEquals(4L, result.getAlertsCountSince());
        assertEquals(1L, result.getResolvedAlertsCountSince());
        assertNotNull(result.getLatestTrustContact());

        verify(analysisRepository, never()).findTop3ByUserEmail(anyString());
        verify(trustContactRepository, never()).findFirstByProtectedUser_EmailOrderByCreatedAtDesc(anyString());
    }

    @Test
    @DisplayName("Debería retornar un objeto con contadores comunes pero sin listados si el rol es desconocido")
    void execute_UnknownRole_ShouldReturnBaseDashboardWithoutLists() {
        // Arrange
        when(analysisRepository.countByUserEmailSince(eq(userEmail), any(LocalDateTime.class))).thenReturn(0L);
        when(alertsRepository.countAlertsByEmailSince(eq(userEmail), any(LocalDateTime.class))).thenReturn(0L);
        when(alertsRepository.countResolvedAlertsByEmailSince(eq(userEmail), any(LocalDateTime.class))).thenReturn(0L);
        when(chatsRepository.findLastUpdatedByUserEmail(userEmail)).thenReturn(Optional.empty());

        // Act
        DashboardData result = getDashboardDataUseCase.execute(userEmail, Role.ADMIN);

        // Assert
        assertNotNull(result, "El objeto DashboardData no debería ser nulo");
        assertEquals(0L, result.getAnalysisCountSince());
        assertEquals(0L, result.getAlertsCountSince());
        assertEquals(0L, result.getResolvedAlertsCountSince());
        assertNull(result.getLatestTrustContact());

        verify(trustContactRepository, never()).findFirstByCarer_EmailOrderByCreatedAtDesc(anyString());
        verify(trustContactRepository, never()).findFirstByProtectedUser_EmailOrderByCreatedAtDesc(anyString());
        verify(analysisRepository, never()).findTop3ByUserEmail(anyString());
        verify(alertsRepository, never()).findTop3ActiveAlertsByCarerEmail(anyString());
    }
}