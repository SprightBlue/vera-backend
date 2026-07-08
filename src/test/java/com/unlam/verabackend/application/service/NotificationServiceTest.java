package com.unlam.verabackend.application.service;

import com.unlam.verabackend.domain.model.Notifications;
import com.unlam.verabackend.domain.model.NotificationsType;
import com.unlam.verabackend.domain.port.out.NotificationsRepository;
import com.unlam.verabackend.domain.port.out.RtcProvider;
import com.unlam.verabackend.infrastructure.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para NotificationService")
class NotificationServiceTest {

    @Mock
    private NotificationsRepository notificationsRepository;

    @Mock
    private RtcProvider rtcProvider;

    @InjectMocks
    private NotificationService notificationService;

    private User targetUser;
    private Map<String, Object> payload;
    private String triggerName;

    @BeforeEach
    void setUp() {
        targetUser = new User();
        targetUser.setId(10L);
        targetUser.setEmail("tutor@unlam.edu.ar");

        triggerName = "Juan Pérez";
        payload = Map.of("alertId", UUID.randomUUID().toString());
    }

    @Test
    @DisplayName("Debería crear, guardar y despachar por RTC una notificación de tipo ALERT con su formato correcto y ID UUID")
    void createAndDispatch_AlertNotification_ShouldSaveAndPublish() {
        // Arrange
        UUID generatedNotificationId = UUID.randomUUID();

        when(notificationsRepository.save(any(Notifications.class))).thenAnswer(inv -> {
            Notifications n = inv.getArgument(0);
            n.setId(generatedNotificationId);
            return n;
        });

        when(notificationsRepository.findUnreadByUserEmail(targetUser.getEmail()))
                .thenReturn(List.of(new Notifications()));

        // Act
        notificationService.createAndDispatch(targetUser, NotificationsType.ALERT, triggerName, payload);

        // Assert
        verify(notificationsRepository, times(1)).save(any(Notifications.class));
        verify(notificationsRepository, times(1)).findUnreadByUserEmail(targetUser.getEmail());

        verify(rtcProvider, times(1)).publishNewNotification(
                eq(targetUser.getEmail()),
                argThat(notification ->
                        generatedNotificationId.equals(notification.getId()) &&
                                "¡Alerta de Seguridad Detectada!".equals(notification.getTitle()) &&
                                "El usuario Juan Pérez ha generado una alerta de riesgo crítico.".equals(notification.getMessage()) &&
                                !notification.isRead()
                ),
                eq(1)
        );
    }

    @Test
    @DisplayName("Debería mapear correctamente los títulos y mensajes para el resto de tipos de la matriz switch")
    void createAndDispatch_RemainingTypes_ShouldMapStringsCorrectly() {
        // Arrange
        when(notificationsRepository.save(any(Notifications.class))).thenAnswer(inv -> {
            Notifications n = inv.getArgument(0);
            n.setId(UUID.randomUUID());
            return n;
        });
        when(notificationsRepository.findUnreadByUserEmail(targetUser.getEmail())).thenReturn(Collections.emptyList());

        // Act & Assert para ALERT_SOLVED
        notificationService.createAndDispatch(targetUser, NotificationsType.ALERT_SOLVED, triggerName, payload);
        verify(rtcProvider).publishNewNotification(any(), argThat(n ->
                "Alerta Resuelta".equals(n.getTitle()) && n.getMessage().contains("marcada como resuelta.")), eq(0));

        // Act & Assert para INVITATION
        notificationService.createAndDispatch(targetUser, NotificationsType.INVITATION, triggerName, payload);
        verify(rtcProvider).publishNewNotification(any(), argThat(n ->
                "Nueva Invitación de Contacto".equals(n.getTitle()) && n.getMessage().contains("solicitud para ser su contacto")), eq(0));

        // Act & Assert para INVITATION_ACCEPTED
        notificationService.createAndDispatch(targetUser, NotificationsType.INVITATION_ACCEPTED, triggerName, payload);
        verify(rtcProvider).publishNewNotification(any(), argThat(n ->
                "Invitación Aceptada".equals(n.getTitle()) && n.getMessage().contains("aceptó tu invitación")), eq(0));

        // Act & Assert para INVITATION_REJECTED
        notificationService.createAndDispatch(targetUser, NotificationsType.INVITATION_REJECTED, triggerName, payload);
        verify(rtcProvider).publishNewNotification(any(), argThat(n ->
                "Invitación Rechazada".equals(n.getTitle()) && n.getMessage().contains("rechazó tu invitación")), eq(0));
    }
}