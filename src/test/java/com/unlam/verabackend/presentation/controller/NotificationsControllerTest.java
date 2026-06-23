package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.application.service.SseService;
import com.unlam.verabackend.domain.port.in.ManageNotificationsUseCase;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.presentation.dto.RegisterNotificationTokenRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class NotificationsControllerTest {

    private ManageNotificationsUseCase useCase;
    private NotificationsController controller;
    private User user;

    @BeforeEach
    void setUp() {
        useCase = mock(ManageNotificationsUseCase.class);
        controller = new NotificationsController(useCase, mock(SseService.class));
        user = new User();
        user.setEmail("test@unlam.edu.ar");
    }

    @Test
    void registerDeviceToken_UsesAuthenticatedUser() {
        RegisterNotificationTokenRequest request = new RegisterNotificationTokenRequest();
        request.setToken("fcm-token");
        request.setPlatform("android");

        ResponseEntity<Void> response = controller.registerDeviceToken(user, request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(useCase).registerDeviceToken(user, "fcm-token", "android");
    }

    @Test
    void registerDeviceToken_WhenUserIsMissing_ReturnsUnauthorized() {
        RegisterNotificationTokenRequest request = new RegisterNotificationTokenRequest();
        request.setToken("fcm-token");
        request.setPlatform("android");

        ResponseEntity<Void> response = controller.registerDeviceToken(null, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verifyNoInteractions(useCase);
    }
}
