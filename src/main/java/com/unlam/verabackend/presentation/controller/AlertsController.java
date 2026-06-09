package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.model.Alerts;
import com.unlam.verabackend.domain.port.in.ManageAlertsUseCase;
import com.unlam.verabackend.presentation.dto.AlertsDetailResponse;
import com.unlam.verabackend.presentation.dto.AlertsResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.unlam.verabackend.infrastructure.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertsController {

    private final ManageAlertsUseCase manageAlertsUseCase;

    public AlertsController(ManageAlertsUseCase manageAlertsUseCase) {
        this.manageAlertsUseCase = manageAlertsUseCase;
    }

    @GetMapping
    public ResponseEntity<Page<AlertsResponse>> getHistoryByCarerEmail(
            // 🚀 PROD: @AuthenticationPrincipal User user,
            // 🛠️ DEV:
            @RequestHeader("user-email") String email,
            @RequestParam(value = "resolved", required = false) Boolean resolved,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        // 🚀 PROD: String email = user.getEmail();

        Page<Alerts> alertsPage = (resolved != null)
                ? manageAlertsUseCase.getHistoryByCarerEmailAndIsResolved(email, resolved, pageable)
                : manageAlertsUseCase.getHistoryByCarerEmail(email, pageable);

        return ResponseEntity.ok(alertsPage.map(AlertsResponse::fromDomain));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertsDetailResponse> getAlertDetail(
            // 🚀 PROD: @AuthenticationPrincipal User user,
            // 🛠️ DEV:
            @RequestHeader("user-email") String email,
            @PathVariable UUID id
    ) {
        // 🚀 PROD: String email = user.getEmail();

        Alerts alert = manageAlertsUseCase.getAlertDetail(id, email);
        return ResponseEntity.ok(AlertsDetailResponse.fromDomain(alert));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlert(
            // 🚀 PROD: @AuthenticationPrincipal User user,
            // 🛠️ DEV:
            @RequestHeader("user-email") String email,
            @PathVariable UUID id
    ) {
        // 🚀 PROD: String email = user.getEmail();

        manageAlertsUseCase.deleteAlert(id, email);
        return ResponseEntity.noContent().build();
    }
}