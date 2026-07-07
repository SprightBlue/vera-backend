package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.application.service.SseService;
import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.Alerts;
import com.unlam.verabackend.domain.model.NotificationsType;
import com.unlam.verabackend.domain.model.RiskLevel;
import com.unlam.verabackend.domain.model.Role;
import com.unlam.verabackend.domain.port.out.AlertsRepository;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManageAlertsUseCaseImplTest {

    @Mock private AlertsRepository alertsRepository;
    @Mock private TrustContactRepository trustContactRepository;
    @Mock private UserRepository userRepository;
    @Mock private SseService sseService;

    @Captor
    private ArgumentCaptor<Map<String, Object>> payloadCaptor;

    @InjectMocks
    private ManageAlertsUseCaseImpl manageAlertsUseCase;

    private String carerEmail;
    private String protectedEmail;
    private User mockCarer;
    private User mockProtectedUser;
    private TrustContact mockTrustContact;
    private Alerts mockAlert;
    private UUID alertId;

    @BeforeEach
    void setUp() {
        carerEmail = "carlos.cuidador@ejemplo.com";
        protectedEmail = "ana.protegida@ejemplo.com";
        alertId = UUID.randomUUID();

        mockCarer = new User();
        mockCarer.setId(101L);
        mockCarer.setEmail(carerEmail);
        mockCarer.setFullName("Carlos Gómez");
        mockCarer.setRole(Role.CARER);

        mockProtectedUser = new User();
        mockProtectedUser.setId(202L);
        mockProtectedUser.setFullName("Abuela Ana");
        mockProtectedUser.setEmail(protectedEmail);
        mockProtectedUser.setRole(Role.PROTECTED);

        mockTrustContact = new TrustContact();
        mockTrustContact.setId(55L);
        mockTrustContact.setCarer(mockCarer);
        mockTrustContact.setProtectedUser(mockProtectedUser);

        mockAlert = mock(Alerts.class);
        lenient().when(mockAlert.getId()).thenReturn(alertId);
        lenient().when(mockAlert.getTrustContact()).thenReturn(mockTrustContact);
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException cuando el email del usuario no está registrado")
    void getAlertsHistory_UserNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findByEmail(carerEmail)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                manageAlertsUseCase.getAlertsHistory(carerEmail, true, RiskLevel.HIGH, "búsqueda", 0)
        );
        verify(userRepository).findByEmail(carerEmail);
        verifyNoInteractions(trustContactRepository, alertsRepository);
    }

    @Test
    @DisplayName("Debe buscar contactos por ProtectedUserId cuando el rol del usuario no es CARER")
    void getAlertsHistory_UserIsProtectedUser_SearchesByProtectedUserId() {
        mockProtectedUser.setRole(Role.PROTECTED);
        when(userRepository.findByEmail(protectedEmail)).thenReturn(Optional.of(mockProtectedUser));
        when(trustContactRepository.findByProtectedUserId(mockProtectedUser.getId())).thenReturn(List.of(mockTrustContact));
        when(alertsRepository.findByCriteria(List.of(55L), true, RiskLevel.HIGH, "test", 0))
                .thenReturn(new PageImpl<>(List.of(mockAlert)));

        Page<Alerts> result = manageAlertsUseCase.getAlertsHistory(protectedEmail, true, RiskLevel.HIGH, "test", 0);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(trustContactRepository).findByProtectedUserId(mockProtectedUser.getId());
        verify(trustContactRepository, never()).findByCarerId(anyLong());
    }

    @Test
    @DisplayName("Debe retornar una página vacía de forma directa si el cuidador no tiene ningún contacto asignado")
    void getAlertsHistory_NoContacts_ReturnsEmptyPage() {
        when(userRepository.findByEmail(carerEmail)).thenReturn(Optional.of(mockCarer));
        when(trustContactRepository.findByCarerId(mockCarer.getId())).thenReturn(Collections.emptyList());

        Page<Alerts> result = manageAlertsUseCase.getAlertsHistory(carerEmail, true, RiskLevel.HIGH, "test", 0);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(alertsRepository, never()).findByCriteria(any(), any(), any(), any(), anyInt());
    }

    @Test
    @DisplayName("Debe retornar la página de alertas invocando al repositorio con la lista de IDs de contactos y criterios")
    void getAlertsHistory_WithContacts_ReturnsPageWithAlerts() {
        List<TrustContact> contacts = List.of(mockTrustContact);
        Page<Alerts> expectedPage = new PageImpl<>(List.of(mockAlert));

        when(userRepository.findByEmail(carerEmail)).thenReturn(Optional.of(mockCarer));
        when(trustContactRepository.findByCarerId(mockCarer.getId())).thenReturn(contacts);
        when(alertsRepository.findByCriteria(List.of(55L), true, RiskLevel.HIGH, "test", 0)).thenReturn(expectedPage);

        Page<Alerts> result = manageAlertsUseCase.getAlertsHistory(carerEmail, true, RiskLevel.HIGH, "test", 0);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(alertsRepository).findByCriteria(List.of(55L), true, RiskLevel.HIGH, "test", 0);
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si la alerta no existe en la base de datos")
    void getAlertDetail_AlertNotFound_ThrowsResourceNotFoundException() {
        when(alertsRepository.findById(alertId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> manageAlertsUseCase.getAlertDetail(alertId, carerEmail));
    }

    @Test
    @DisplayName("Debe lanzar AccessDeniedException (403) si un tercero intenta husmear una alerta que no le corresponde")
    void getAlertDetail_AccessDenied_ThrowsAccessDeniedException() {
        when(alertsRepository.findById(alertId)).thenReturn(Optional.of(mockAlert));

        assertThrows(AccessDeniedException.class, () -> manageAlertsUseCase.getAlertDetail(alertId, "intruso@ejemplo.com"));
    }

    @Test
    @DisplayName("Debe retornar la alerta exitosamente si el solicitante es el cuidador asignado")
    void getAlertDetail_SuccessForCarer_ReturnsAlert() {
        when(alertsRepository.findById(alertId)).thenReturn(Optional.of(mockAlert));

        Alerts result = manageAlertsUseCase.getAlertDetail(alertId, carerEmail);

        assertNotNull(result);
        assertEquals(alertId, result.getId());
    }

    @Test
    @DisplayName("Debe retornar la alerta exitosamente si el solicitante es el usuario protegido")
    void getAlertDetail_SuccessForProtectedUser_ReturnsAlert() {
        when(alertsRepository.findById(alertId)).thenReturn(Optional.of(mockAlert));

        Alerts result = manageAlertsUseCase.getAlertDetail(alertId, protectedEmail);

        assertNotNull(result);
        assertEquals(alertId, result.getId());
    }

    @Test
    @DisplayName("Debe eliminar la alerta llamando al repositorio si el solicitante es el cuidador asignado")
    void deleteAlert_Success() {
        when(alertsRepository.findById(alertId)).thenReturn(Optional.of(mockAlert));

        assertDoesNotThrow(() -> manageAlertsUseCase.deleteAlert(alertId, carerEmail));

        verify(alertsRepository).deleteById(alertId);
    }

    @Test
    @DisplayName("Debe lanzar AccessDeniedException al eliminar si el solicitante es el usuario protegido y no el cuidador")
    void deleteAlert_ByProtectedUser_ThrowsAccessDeniedException() {
        when(alertsRepository.findById(alertId)).thenReturn(Optional.of(mockAlert));

        assertThrows(AccessDeniedException.class, () -> manageAlertsUseCase.deleteAlert(alertId, protectedEmail));
        verify(alertsRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Debe marcar la alerta como resuelta enviando los datos correspondientes en el evento SSE al usuario protegido")
    void resolveAlert_Success_ResolvesAndSendsNotification() {
        when(alertsRepository.findById(alertId)).thenReturn(Optional.of(mockAlert));

        manageAlertsUseCase.resolveAlert(alertId, carerEmail);

        verify(alertsRepository).resolveAlert(eq(alertId), any(LocalDateTime.class));

        verify(sseService).createAndSendNotification(
                eq(mockTrustContact.getProtectedUser()),
                eq(NotificationsType.ALERT_SOLVED),
                eq("Carlos Gómez"),
                payloadCaptor.capture()
        );

        Map<String, Object> capturedPayload = payloadCaptor.getValue();
        assertNotNull(capturedPayload);
        assertEquals(alertId.toString(), capturedPayload.get("alertId"));
    }

    @Test
    @DisplayName("Debe lanzar AccessDeniedException al resolver si el solicitante es el usuario protegido y no el cuidador")
    void resolveAlert_ByProtectedUser_ThrowsAccessDeniedException() {
        when(alertsRepository.findById(alertId)).thenReturn(Optional.of(mockAlert));

        assertThrows(AccessDeniedException.class, () -> manageAlertsUseCase.resolveAlert(alertId, protectedEmail));
        verify(alertsRepository, never()).resolveAlert(any(), any());
        verifyNoInteractions(sseService);
    }
}