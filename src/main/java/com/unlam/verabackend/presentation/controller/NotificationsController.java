package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.model.Notifications;
import com.unlam.verabackend.domain.port.in.ManageNotificationsUseCase;
import com.unlam.verabackend.application.service.SseService;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.presentation.dto.PagedResponse;
import com.unlam.verabackend.presentation.dto.NotificationsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

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
        return sseService.createEmitter(email);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<NotificationsResponse>> getMyNotifications(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        String email = user.getEmail();

        Page<Notifications> page = useCase.getMyNotifications(email, pageable);

        PagedResponse<NotificationsResponse> response = PagedResponse.fromPage(
                page,
                NotificationsResponse::fromDomain
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal User user
    ) {
        String email = user.getEmail();

        useCase.markAllMyNotificationsAsRead(email);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id
    ) {
        String email = user.getEmail();

        useCase.deleteNotification(id, email);
        return ResponseEntity.noContent().build();
    }
}