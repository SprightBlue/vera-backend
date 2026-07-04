package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.model.Alerts;
import com.unlam.verabackend.domain.model.RiskLevel;
import com.unlam.verabackend.domain.port.in.ManageAlertsUseCase;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.presentation.dto.AlertsDetailResponse;
import com.unlam.verabackend.presentation.dto.AlertsResponse;
import com.unlam.verabackend.presentation.dto.PagedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertsController {

    private final ManageAlertsUseCase manageAlertsUseCase;

    @GetMapping
    public ResponseEntity<PagedResponse<AlertsResponse>> getAlertsHistory(
            @AuthenticationPrincipal User user,
            @RequestParam(value = "resolved", required = false) Boolean resolved,
            @RequestParam(value = "riskLevel", required = false) RiskLevel riskLevel,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page
    ) {
        log.info("Buscando historial de alertas para el cuidador: {} - Filtros [resolved: {}, riskLevel: {}, search: {}, page: {}]",
                user.getEmail(), resolved, riskLevel, search, page);

        Page<Alerts> alertsPage = manageAlertsUseCase.getAlertsHistory(
                user.getEmail(), resolved, riskLevel, search, page
        );

        return ResponseEntity.ok(PagedResponse.fromPage(alertsPage, AlertsResponse::fromDomain));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertsDetailResponse> getAlertDetail(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id
    ) {
        log.info("Solicitando detalle de alerta ID: {} por usuario: {}", id, user.getEmail());
        Alerts alert = manageAlertsUseCase.getAlertDetail(id, user.getEmail());
        return ResponseEntity.ok(AlertsDetailResponse.fromDomain(alert));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlert(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id
    ) {
        log.info("Solicitud para eliminar alerta ID: {} por usuario: {}", id, user.getEmail());
        manageAlertsUseCase.deleteAlert(id, user.getEmail());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<Void> resolveAlert(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id
    ) {
        log.info("Solicitud para resolver alerta ID: {} por usuario: {}", id, user.getEmail());
        manageAlertsUseCase.resolveAlert(id, user.getEmail());
        return ResponseEntity.noContent().build();
    }
}