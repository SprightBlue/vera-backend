package com.unlam.verabackend.application.service;

import com.unlam.verabackend.domain.model.Notifications;
import com.unlam.verabackend.domain.model.NotificationsType;
import com.unlam.verabackend.domain.port.out.NotificationsRepository;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.provider.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SseServiceTest {

    @Mock
    private NotificationsRepository repository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private SseService sseService;

    private User sampleUser;
    private final String userEmail = "test@unlam.edu.ar";

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setEmail(userEmail);
    }

    @ParameterizedTest
    @EnumSource(NotificationsType.class)
    @DisplayName("Debe persistir la notificación construyendo títulos y mensajes legibles según el tipo enumerado")
    void createAndSendNotification_ForAllTypes_ShouldSaveAndBuildCorrectTexts(NotificationsType type) {
        String triggeringUser = "Carlos Gómez";
        Map<String, Object> payload = new HashMap<>();
        payload.put("details", "Intento de inicio de sesión sospechoso");

        when(repository.save(any(Notifications.class))).thenAnswer(invocation -> invocation.getArgument(0));

        sseService.createAndSendNotification(sampleUser, type, triggeringUser, payload);

        ArgumentCaptor<Notifications> notificationCaptor = ArgumentCaptor.forClass(Notifications.class);
        verify(repository, times(1)).save(notificationCaptor.capture());

        Notifications captured = notificationCaptor.getValue();
        assertEquals(sampleUser, captured.getUser());
        assertEquals(type, captured.getType());
        assertFalse(captured.isRead());
        assertNotNull(captured.getCreatedAt());

        if (type == NotificationsType.ALERT) {
            verify(emailService, times(1)).enviarEmailAlertaRiesgoAlto(eq(userEmail), eq(triggeringUser), eq("Intento de inicio de sesión sospechoso"));
        } else {
            verify(emailService, never()).enviarEmailAlertaRiesgoAlto(anyString(), anyString(), anyString());
        }

        switch (type) {
            case ALERT -> {
                assertEquals("¡Alerta de Seguridad Detectada!", captured.getTitle());
                assertEquals("El usuario Carlos Gómez ha generado una alerta de riesgo crítico.", captured.getMessage());
            }
            case ALERT_SOLVED -> {
                assertEquals("Alerta Resuelta", captured.getTitle());
                assertEquals("La alerta de Carlos Gómez ha sido marcada como resuelta.", captured.getMessage());
            }
            case INVITATION -> {
                assertEquals("Nueva Invitación de Contacto", captured.getTitle());
                assertEquals("Carlos Gómez te ha enviado una solicitud para ser su contacto de confianza.", captured.getMessage());
            }
            case INVITATION_ACCEPTED -> {
                assertEquals("Invitación Aceptada", captured.getTitle());
                assertEquals("Carlos Gómez aceptó tu invitación de confianza.", captured.getMessage());
            }
            case INVITATION_REJECTED -> {
                assertEquals("Invitación Rechazada", captured.getTitle());
                assertEquals("Carlos Gómez rechazó tu invitación de confianza.", captured.getMessage());
            }
        }
    }

    @Test
    @DisplayName("Debe despachar el correo de alerta con un mensaje predeterminado si el payload no contiene detalles específicos")
    void createAndSendNotification_WhenAlertPayloadLacksDetailsKey_ShouldUseDefaultTextMessage() {
        when(repository.save(any(Notifications.class))).thenAnswer(invocation -> invocation.getArgument(0));

        sseService.createAndSendNotification(sampleUser, NotificationsType.ALERT, "Test User", Collections.emptyMap());

        verify(emailService, times(1)).enviarEmailAlertaRiesgoAlto(
                eq(userEmail),
                eq("Test User"),
                eq("Se detectó actividad sospechosa que requiere tu revisión inmediata.")
        );
    }
}