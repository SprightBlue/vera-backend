package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.model.Notifications;
import com.unlam.verabackend.domain.port.in.ManageNotificationsUseCase;
import com.unlam.verabackend.application.service.SseService;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.presentation.dto.PagedResponse;
import com.unlam.verabackend.presentation.dto.NotificationsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationsController {

    private final ManageNotificationsUseCase useCase;
    private final SseService sseService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications(
            @AuthenticationPrincipal User user
    ) {
        String email = user.getEmail();
        log.info("[HTTP GET] Solicitando apertura de canal SSE para el usuario: {}", email);
        return sseService.createEmitter(email);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<NotificationsResponse>> getMyNotifications(
            @AuthenticationPrincipal User user,
            Pageable pageable
    ) {
        String email = user.getEmail();
        log.info("[HTTP GET] Solicitando listado de notificaciones para: {}. Página solicitada: {}", email, pageable.getPageNumber());

        Page<Notifications> page = useCase.getMyNotifications(email, pageable);

        PagedResponse<NotificationsResponse> response = PagedResponse.fromPage(
                page,
                NotificationsResponse::fromDomain
        );

        log.debug("Retornando {} notificaciones de la página {} para el usuario: {}", page.getNumberOfElements(), page.getNumber(), email);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal User user
    ) {
        String email = user.getEmail();
        log.info("[HTTP PATCH] Solicitud para marcar todas las notificaciones como leídas de: {}", email);

        useCase.markAllMyNotificationsAsRead(email);

        log.info("Todas las notificaciones de {} fueron marcadas como leídas con éxito.", email);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id
    ) {
        String email = user.getEmail();
        log.info("[HTTP DELETE] Solicitud para eliminar notificación ID: {} por el usuario: {}", id, email);

        useCase.deleteNotification(id, email);

        log.info("Notificación ID: {} eliminada correctamente vía endpoint.", id);
        return ResponseEntity.noContent().build();
    }
}