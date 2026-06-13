package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.model.Notifications;
import com.unlam.verabackend.domain.port.in.ManageNotificationsUseCase;
import com.unlam.verabackend.application.service.SseService;
import com.unlam.verabackend.presentation.dto.PagedResponse;
import com.unlam.verabackend.presentation.dto.NotificationsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
            // 🚀 PROD: @AuthenticationPrincipal User user,
            @RequestHeader("user-email") String email
    ) {
        // 🚀 PROD: String email = user.getEmail();
        return sseService.createEmitter(email);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<NotificationsResponse>> getMyNotifications(
            // 🚀 PROD: @AuthenticationPrincipal User user,
            @RequestHeader("user-email") String email,
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        // 🚀 PROD: String email = user.getEmail();

        Page<Notifications> page = useCase.getMyNotifications(email, pageable);

        // Transformamos usando el método genérico
        PagedResponse<NotificationsResponse> response = PagedResponse.fromPage(
                page,
                NotificationsResponse::fromDomain
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            // 🚀 PROD: @AuthenticationPrincipal User user,
            @RequestHeader("user-email") String email
    ) {
        // 🚀 PROD: String email = user.getEmail();

        useCase.markAllMyNotificationsAsRead(email);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            // 🚀 PROD: @AuthenticationPrincipal User user,
            @RequestHeader("user-email") String email,
            @PathVariable UUID id
    ) {
        // 🚀 PROD: String email = user.getEmail();

        useCase.deleteNotification(id, email);
        return ResponseEntity.noContent().build();
    }
}