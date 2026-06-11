package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.application.service.NotificationService;
import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.Alerts;
import com.unlam.verabackend.domain.model.NotificationsType;
import com.unlam.verabackend.domain.port.out.AlertsRepository;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManageAlertsUseCaseImplTest {

    @Mock
    private AlertsRepository alertsRepository;
    @Mock
    private TrustContactRepository trustContactRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ManageAlertsUseCaseImpl manageAlertsUseCase;

    private String carerEmail;
    private Pageable pageable;
    private User mockCarer;
    private TrustContact mockTrustContact;
    private Alerts mockAlert;
    private UUID alertId;

    @BeforeEach
    void setUp() {
        carerEmail = "carlos.cuidador@ejemplo.com";
        pageable = PageRequest.of(0, 10);
        alertId = UUID.randomUUID();

        mockCarer = new User();
        mockCarer.setId(101L);
        mockCarer.setEmail(carerEmail);
        mockCarer.setFullName("Carlos Gómez");

        User protectedUser = new User();
        protectedUser.setId(202L);
        protectedUser.setEmail("ana.protegida@ejemplo.com");

        mockTrustContact = new TrustContact();
        mockTrustContact.setId(55L);
        mockTrustContact.setCarer(mockCarer);
        mockTrustContact.setProtectedUser(protectedUser);

        mockAlert = mock(Alerts.class);
        lenient().when(mockAlert.getId()).thenReturn(alertId);
        lenient().when(mockAlert.getTrustContact()).thenReturn(mockTrustContact);
    }

    // ==========================================
    // TESTS: Métodos Auxiliares / Casos Comunes
    // ==========================================

    @Test
    void getTrustContactIdsByEmail_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(userRepository.findByEmail(carerEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> manageAlertsUseCase.getHistoryByCarerEmail(carerEmail, pageable));
        verify(userRepository).findByEmail(carerEmail);
        verifyNoInteractions(trustContactRepository, alertsRepository);
    }

    // ==========================================
    // TESTS: getHistoryByCarerEmail
    // ==========================================

    @Test
    void getHistoryByCarerEmail_NoContacts_ReturnsEmptyPage() {
        // Arrange
        when(userRepository.findByEmail(carerEmail)).thenReturn(Optional.of(mockCarer));
        when(trustContactRepository.findByCarerId(mockCarer.getId())).thenReturn(Collections.emptyList());

        // Act
        Page<Alerts> result = manageAlertsUseCase.getHistoryByCarerEmail(carerEmail, pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(alertsRepository, never()).findByTrustContactIdsCreatedAtDesc(any(), any());
    }

    @Test
    void getHistoryByCarerEmail_WithContacts_ReturnsPageWithAlerts() {
        // Arrange
        List<TrustContact> contacts = List.of(mockTrustContact);
        Page<Alerts> expectedPage = new PageImpl<>(List.of(mockAlert));

        when(userRepository.findByEmail(carerEmail)).thenReturn(Optional.of(mockCarer));
        when(trustContactRepository.findByCarerId(mockCarer.getId())).thenReturn(contacts);
        when(alertsRepository.findByTrustContactIdsCreatedAtDesc(List.of(55L), pageable)).thenReturn(expectedPage);

        // Act
        Page<Alerts> result = manageAlertsUseCase.getHistoryByCarerEmail(carerEmail, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(alertsRepository).findByTrustContactIdsCreatedAtDesc(List.of(55L), pageable);
    }

    // ==========================================
    // TESTS: getHistoryByCarerEmailAndIsResolved
    // ==========================================

    @Test
    void getHistoryByCarerEmailAndIsResolved_NoContacts_ReturnsEmptyPage() {
        // Arrange
        when(userRepository.findByEmail(carerEmail)).thenReturn(Optional.of(mockCarer));
        when(trustContactRepository.findByCarerId(mockCarer.getId())).thenReturn(Collections.emptyList());

        // Act
        Page<Alerts> result = manageAlertsUseCase.getHistoryByCarerEmailAndIsResolved(carerEmail, true, pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(alertsRepository, never()).findByTrustContactIdsAndIsResolvedCreatedAtDesc(any(), anyBoolean(), any());
    }

    @Test
    void getHistoryByCarerEmailAndIsResolved_WithContacts_ReturnsPageWithAlerts() {
        // Arrange
        List<TrustContact> contacts = List.of(mockTrustContact);
        Page<Alerts> expectedPage = new PageImpl<>(List.of(mockAlert));

        when(userRepository.findByEmail(carerEmail)).thenReturn(Optional.of(mockCarer));
        when(trustContactRepository.findByCarerId(mockCarer.getId())).thenReturn(contacts);
        when(alertsRepository.findByTrustContactIdsAndIsResolvedCreatedAtDesc(List.of(55L), true, pageable)).thenReturn(expectedPage);

        // Act
        Page<Alerts> result = manageAlertsUseCase.getHistoryByCarerEmailAndIsResolved(carerEmail, true, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(alertsRepository).findByTrustContactIdsAndIsResolvedCreatedAtDesc(List.of(55L), true, pageable);
    }

    // ==========================================
    // TESTS: getAlertDetail
    // ==========================================

    @Test
    void getAlertDetail_AlertNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(alertsRepository.findById(alertId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> manageAlertsUseCase.getAlertDetail(alertId, carerEmail));
    }

    @Test
    void getAlertDetail_AccessDenied_ThrowsAccessDeniedException() {
        // Arrange
        User wrongCarer = new User();
        wrongCarer.setEmail("otro.cuidador@ejemplo.com");
        mockTrustContact.setCarer(wrongCarer);

        when(alertsRepository.findById(alertId)).thenReturn(Optional.of(mockAlert));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> manageAlertsUseCase.getAlertDetail(alertId, carerEmail));
    }

    @Test
    void getAlertDetail_Success_ReturnsAlert() {
        // Arrange
        when(alertsRepository.findById(alertId)).thenReturn(Optional.of(mockAlert));

        // Act
        Alerts result = manageAlertsUseCase.getAlertDetail(alertId, carerEmail);

        // Assert
        assertNotNull(result);
        assertEquals(alertId, result.getId());
    }

    // ==========================================
    // TESTS: deleteAlert
    // ==========================================

    @Test
    void deleteAlert_Success() {
        // Arrange
        when(alertsRepository.findById(alertId)).thenReturn(Optional.of(mockAlert));

        // Act
        manageAlertsUseCase.deleteAlert(alertId, carerEmail);

        // Assert
        verify(alertsRepository).deleteById(alertId);
    }

    // ==========================================
    // TESTS: resolveAlert
    // ==========================================

    @Test
    void resolveAlert_Success_ResolvesAndSendsNotification() {
        // Arrange
        when(alertsRepository.findById(alertId)).thenReturn(Optional.of(mockAlert));

        // Act
        manageAlertsUseCase.resolveAlert(alertId, carerEmail);

        // Assert
        verify(mockAlert).resolve();
        verify(alertsRepository).save(mockAlert, 55L);
        verify(notificationService).createAndSendNotification(
                eq(mockTrustContact.getProtectedUser()),
                eq(NotificationsType.ALERT_SOLVED),
                eq("Carlos Gómez"),
                anyMap()
        );
    }
}