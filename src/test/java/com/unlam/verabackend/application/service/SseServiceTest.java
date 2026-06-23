package com.unlam.verabackend.application.service;

import com.unlam.verabackend.domain.model.Notifications;
import com.unlam.verabackend.domain.model.NotificationsType;
import com.unlam.verabackend.domain.port.out.NotificationsRepository;
import com.unlam.verabackend.domain.port.out.PushNotificationSender;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.provider.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SseServiceTest {

    @Mock
    private NotificationsRepository repository;

    @Mock
    private EmailService emailService;

    @Mock
    private PushNotificationSender pushNotificationSender;

    @InjectMocks
    private SseService sseService;

    private User sampleUser;
    private final String userEmail = "test@unlam.edu.ar";

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setEmail(userEmail);
    }

    @Test
    void createEmitter_WhenCalled_ShouldReturnEmitterAndSendInitEvent() {
        SseEmitter emitter = sseService.createEmitter(userEmail);
        assertNotNull(emitter);
        assertEquals(1800000L, emitter.getTimeout());
    }

    @Test
    void createEmitter_WhenEmitterCallbacksAreTriggered_ShouldExecuteRemovals() {
        SseEmitter emitter = sseService.createEmitter(userEmail);
        emitter.complete();
        assertDoesNotThrow(() -> sseService.createEmitter(userEmail));
    }

    @Test
    void createAndSendNotification_WhenEmitterThrowsExceptionOnInit_ShouldIgnoreException() {
        SseService serviceWithMockEmitter = new SseService(repository, emailService, pushNotificationSender);
        SseEmitter emitter = serviceWithMockEmitter.createEmitter(userEmail);
        assertNotNull(emitter);
    }

    @ParameterizedTest
    @EnumSource(NotificationsType.class)
    void createAndSendNotification_ForAllTypes_ShouldSaveAndBuildCorrectTexts(NotificationsType type) {
        String triggeringUser = "Juan Pérez";
        Map<String, Object> payload = new HashMap<>();
        payload.put("details", "Detalle simulado");

        when(repository.save(any(Notifications.class))).thenAnswer(invocation -> invocation.getArgument(0));

        sseService.createAndSendNotification(sampleUser, type, triggeringUser, payload);

        ArgumentCaptor<Notifications> notificationCaptor = ArgumentCaptor.forClass(Notifications.class);
        verify(repository, times(1)).save(notificationCaptor.capture());
        verify(pushNotificationSender, times(1)).send(any(Notifications.class));

        Notifications captured = notificationCaptor.getValue();
        assertEquals(sampleUser, captured.getUser());
        assertEquals(type, captured.getType());
        assertFalse(captured.isRead());
        assertNotNull(captured.getCreatedAt());

        if (type == NotificationsType.ALERT) {
            verify(emailService, times(1)).enviarEmailAlertaRiesgoAlto(eq(userEmail), eq(triggeringUser), eq("Detalle simulado"));
        } else {
            verify(emailService, never()).enviarEmailAlertaRiesgoAlto(anyString(), anyString(), anyString());
        }

        switch (type) {
            case ALERT -> {
                assertEquals("¡Alerta de Seguridad Detectada!", captured.getTitle());
                assertEquals("El usuario Juan Pérez ha generado una alerta de riesgo crítico.", captured.getMessage());
            }
            case ALERT_SOLVED -> {
                assertEquals("Alerta Resuelta", captured.getTitle());
                assertEquals("La alerta de Juan Pérez ha sido marcada como resuelta.", captured.getMessage());
            }
            case INVITATION -> {
                assertEquals("Nueva Invitación de Contacto", captured.getTitle());
                assertEquals("Juan Pérez te ha enviado una solicitud para ser su contacto de confianza.", captured.getMessage());
            }
            case INVITATION_ACCEPTED -> {
                assertEquals("Invitación Aceptada", captured.getTitle());
                assertEquals("Juan Pérez aceptó tu invitación de confianza.", captured.getMessage());
            }
            case INVITATION_REJECTED -> {
                assertEquals("Invitación Rechazada", captured.getTitle());
                assertEquals("Juan Pérez rechazó tu invitación de confianza.", captured.getMessage());
            }
        }
    }

    @Test
    void createAndSendNotification_WhenEmitterExistsAndWorks_ShouldSendSseSuccessfully() {
        sseService.createEmitter(userEmail);

        Notifications savedNotification = Notifications.builder().title("Test").build();
        when(repository.save(any(Notifications.class))).thenReturn(savedNotification);

        assertDoesNotThrow(() ->
                sseService.createAndSendNotification(sampleUser, NotificationsType.ALERT, "Test", null)
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void createAndSendNotification_WhenEmitterThrowsIOException_ShouldCatchExceptionAndRemoveEmitter() throws IOException {
        SseEmitter mockEmitter = Mockito.mock(SseEmitter.class);
        doThrow(new IOException("Canal roto")).when(mockEmitter).send(any(SseEmitter.SseEventBuilder.class));

        Map<String, SseEmitter> emitters = (Map<String, SseEmitter>) ReflectionTestUtils.getField(sseService, "userEmitters");
        emitters.put(userEmail, mockEmitter);

        Notifications savedNotification = Notifications.builder().title("Fallo").build();
        when(repository.save(any(Notifications.class))).thenReturn(savedNotification);

        assertDoesNotThrow(() ->
                sseService.createAndSendNotification(sampleUser, NotificationsType.ALERT, "Test", null)
        );

        assertFalse(emitters.containsKey(userEmail));
    }

    @Test
    void createAndSendNotification_WhenPushSenderFails_ShouldNotRollbackNotificationSave() {
        Notifications savedNotification = Notifications.builder()
                .user(sampleUser)
                .title("Guardada")
                .build();
        when(repository.save(any(Notifications.class))).thenReturn(savedNotification);
        doThrow(new RuntimeException("firebase down")).when(pushNotificationSender).send(savedNotification);

        assertDoesNotThrow(() ->
                sseService.createAndSendNotification(sampleUser, NotificationsType.INVITATION, "Test", null)
        );

        verify(repository).save(any(Notifications.class));
        verify(pushNotificationSender).send(savedNotification);
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendDeleteEvent_WhenEmitterExistsAndWorks_ShouldSendEventSuccessfully() throws IOException {
        UUID notificationId = UUID.randomUUID();
        SseEmitter mockEmitter = Mockito.mock(SseEmitter.class);

        Map<String, SseEmitter> emitters = (Map<String, SseEmitter>) ReflectionTestUtils.getField(sseService, "userEmitters");
        emitters.put(userEmail, mockEmitter);

        assertDoesNotThrow(() -> sseService.sendDeleteEvent(userEmail, notificationId));

        verify(mockEmitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
        assertTrue(emitters.containsKey(userEmail));
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendDeleteEvent_WhenEmitterThrowsIOException_ShouldCatchExceptionAndRemoveEmitter() throws IOException {
        UUID notificationId = UUID.randomUUID();
        SseEmitter mockEmitter = Mockito.mock(SseEmitter.class);

        doThrow(new IOException("Canal de borrado interrumpido")).when(mockEmitter).send(any(SseEmitter.SseEventBuilder.class));

        Map<String, SseEmitter> emitters = (Map<String, SseEmitter>) ReflectionTestUtils.getField(sseService, "userEmitters");
        emitters.put(userEmail, mockEmitter);

        assertDoesNotThrow(() -> sseService.sendDeleteEvent(userEmail, notificationId));

        verify(mockEmitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
        assertFalse(emitters.containsKey(userEmail));
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendDeleteEvent_WhenEmitterDoesNotExist_ShouldDoNothing() {
        UUID notificationId = UUID.randomUUID();

        Map<String, SseEmitter> emitters = (Map<String, SseEmitter>) ReflectionTestUtils.getField(sseService, "userEmitters");
        emitters.remove(userEmail);

        assertDoesNotThrow(() -> sseService.sendDeleteEvent(userEmail, notificationId));
    }
}
