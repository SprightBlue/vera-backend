package com.unlam.verabackend.application.service;

import com.unlam.verabackend.domain.model.Notifications;
import com.unlam.verabackend.domain.model.NotificationsType;
import com.unlam.verabackend.domain.port.out.NotificationsRepository;
import com.unlam.verabackend.infrastructure.entity.User;
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

    @InjectMocks
    private SseService sseService;

    private User sampleUser;
    private final String userEmail = "test@unlam.edu.ar";

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setEmail(userEmail);
    }

    // ==========================================
    // Tests para createEmitter()
    // ==========================================

    @Test
    void createEmitter_WhenCalled_ShouldReturnEmitterAndSendInitEvent() {
        // Arrange & Act
        SseEmitter emitter = sseService.createEmitter(userEmail);

        // Assert
        assertNotNull(emitter);
        assertEquals(1800000L, emitter.getTimeout());
    }

    @Test
    void createEmitter_WhenEmitterCallbacksAreTriggered_ShouldExecuteRemovals() {
        // Arrange & Act
        SseEmitter emitter = sseService.createEmitter(userEmail);

        emitter.complete();

        // Assert
        assertDoesNotThrow(() -> sseService.createEmitter(userEmail));
    }

    @Test
    void createAndSendNotification_WhenEmitterThrowsExceptionOnInit_ShouldIgnoreException() {
        // Arrange
        SseService serviceWithMockEmitter = new SseService(repository);

        // Act
        SseEmitter emitter = serviceWithMockEmitter.createEmitter(userEmail);

        // Assert
        assertNotNull(emitter);
    }

    // ==========================================
    // Tests para createAndSendNotification() y Switch Blocks
    // ==========================================

    @ParameterizedTest
    @EnumSource(NotificationsType.class)
    void createAndSendNotification_ForAllTypes_ShouldSaveAndBuildCorrectTexts(NotificationsType type) {
        // Arrange
        String triggeringUser = "Juan Pérez";
        Map<String, Object> payload = new HashMap<>();

        when(repository.save(any(Notifications.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        sseService.createAndSendNotification(sampleUser, type, triggeringUser, payload);

        // Assert
        ArgumentCaptor<Notifications> notificationCaptor = ArgumentCaptor.forClass(Notifications.class);
        verify(repository, times(1)).save(notificationCaptor.capture());

        Notifications captured = notificationCaptor.getValue();
        assertEquals(sampleUser, captured.getUser());
        assertEquals(type, captured.getType());
        assertFalse(captured.isRead());
        assertNotNull(captured.getCreatedAt());

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

    // ==========================================
    // Tests para sendSse() (Lógica interna y Errores de canal)
    // ==========================================

    @Test
    void createAndSendNotification_WhenEmitterExistsAndWorks_ShouldSendSseSuccessfully() {
        // Arrange
        sseService.createEmitter(userEmail);

        Notifications savedNotification = Notifications.builder().title("Test").build();
        when(repository.save(any(Notifications.class))).thenReturn(savedNotification);

        // Act & Assert
        assertDoesNotThrow(() ->
                sseService.createAndSendNotification(sampleUser, NotificationsType.ALERT, "Test", null)
        );
    }

    @Test
    void createAndSendNotification_WhenEmitterThrowsIOException_ShouldCatchExceptionAndRemoveEmitter() throws IOException {
        // Arrange
        SseEmitter mockEmitter = Mockito.mock(SseEmitter.class);

        doThrow(new IOException("Canal roto"))
                .when(mockEmitter)
                .send(any(SseEmitter.SseEventBuilder.class));

        sseService.userEmitters.put(userEmail, mockEmitter);

        Notifications savedNotification = Notifications.builder().title("Fallo").build();
        when(repository.save(any(Notifications.class))).thenReturn(savedNotification);

        // Act & Assert
        assertDoesNotThrow(() ->
                sseService.createAndSendNotification(sampleUser, NotificationsType.ALERT, "Test", null)
        );

        assertFalse(sseService.userEmitters.containsKey(userEmail), "El emisor debió ser removido tras la IOException");
    }

    // ==========================================
    // Tests para sendDeleteEvent()
    // ==========================================

    @Test
    void sendDeleteEvent_WhenEmitterExistsAndWorks_ShouldSendEventSuccessfully() throws IOException {
        // Arrange
        UUID notificationId = UUID.randomUUID();
        SseEmitter mockEmitter = Mockito.mock(SseEmitter.class);

        sseService.userEmitters.put(userEmail, mockEmitter);

        // Act & Assert
        assertDoesNotThrow(() -> sseService.sendDeleteEvent(userEmail, notificationId));

        verify(mockEmitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
        assertTrue(sseService.userEmitters.containsKey(userEmail), "El emisor debería seguir activo si el envío fue exitoso");
    }

    @Test
    void sendDeleteEvent_WhenEmitterThrowsIOException_ShouldCatchExceptionAndRemoveEmitter() throws IOException {
        // Arrange
        UUID notificationId = UUID.randomUUID();
        SseEmitter mockEmitter = Mockito.mock(SseEmitter.class);

        doThrow(new IOException("Canal de borrado interrumpido"))
                .when(mockEmitter)
                .send(any(SseEmitter.SseEventBuilder.class));

        sseService.userEmitters.put(userEmail, mockEmitter);

        // Act & Assert
        assertDoesNotThrow(() -> sseService.sendDeleteEvent(userEmail, notificationId));

        verify(mockEmitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
        assertFalse(sseService.userEmitters.containsKey(userEmail), "El emisor debió ser removido de la caché tras la IOException");
    }

    @Test
    void sendDeleteEvent_WhenEmitterDoesNotExist_ShouldDoNothing() {
        // Arrange
        UUID notificationId = UUID.randomUUID();

        sseService.userEmitters.remove(userEmail);

        // Act & Assert
        assertDoesNotThrow(() -> sseService.sendDeleteEvent(userEmail, notificationId));
    }
}