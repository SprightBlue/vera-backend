package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.model.Alerts;
import com.unlam.verabackend.domain.port.in.ManageAlertsUseCase;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.presentation.dto.PagedResponse;
import com.unlam.verabackend.presentation.dto.AlertsDetailResponse;
import com.unlam.verabackend.presentation.dto.AlertsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertsController {

    private final ManageAlertsUseCase manageAlertsUseCase;

    @GetMapping
    public ResponseEntity<PagedResponse<AlertsResponse>> getHistoryByCarerEmail(
            @AuthenticationPrincipal User user,
            @RequestParam(value = "resolved", required = false) Boolean resolved,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        String email = user.getEmail();

        Page<Alerts> alertsPage = (resolved != null)
                ? manageAlertsUseCase.getHistoryByCarerEmailAndIsResolved(email, resolved, pageable)
                : manageAlertsUseCase.getHistoryByCarerEmail(email, pageable);

        PagedResponse<AlertsResponse> response = PagedResponse.fromPage(
                alertsPage,
                AlertsResponse::fromDomain
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertsDetailResponse> getAlertDetail(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id
    ) {
        String email = user.getEmail();

        Alerts alert = manageAlertsUseCase.getAlertDetail(id, email);
        return ResponseEntity.ok(AlertsDetailResponse.fromDomain(alert));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlert(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id
    ) {
        String email = user.getEmail();

        manageAlertsUseCase.deleteAlert(id, email);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<Void> resolveAlert(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id
    ) {
       String email = user.getEmail();

        manageAlertsUseCase.resolveAlert(id, email);
        return ResponseEntity.noContent().build();
    }
}