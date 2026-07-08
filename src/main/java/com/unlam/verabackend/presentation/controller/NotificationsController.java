package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.model.Notifications;
import com.unlam.verabackend.domain.port.in.ManageNotificationsUseCase;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.presentation.dto.PagedResponse;
import com.unlam.verabackend.presentation.dto.NotificationsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationsController {

    private final ManageNotificationsUseCase useCase;

    @GetMapping
    public ResponseEntity<PagedResponse<NotificationsResponse>> getMyNotifications(
            @AuthenticationPrincipal User user,
            Pageable pageable
    ) {
        log.info("REST Request: GET - Consultando catálogo de notificaciones paginadas para el usuario [{}]", user.getEmail());

        Page<Notifications> page = useCase.getMyNotifications(user.getEmail(), pageable);

        log.debug("REST Response: Retornando {} elementos mapeados a la UI.", page.getNumberOfElements());
        return ResponseEntity.ok(convertToPagedResponse(page));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal User user) {
        log.info("REST Request: PATCH - Solicitando marcado de lectura masivo para [{}]", user.getEmail());

        useCase.markAllMyNotificationsAsRead(user.getEmail());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        log.info("REST Request: DELETE - Solicitando remoción de notificación ID [{}] por el operador [{}]", id, user.getEmail());

        useCase.deleteNotification(id, user.getEmail());
        return ResponseEntity.noContent().build();
    }

    private PagedResponse<NotificationsResponse> convertToPagedResponse(Page<Notifications> page) {
        return PagedResponse.fromPage(page, NotificationsResponse::fromDomain);
    }
}