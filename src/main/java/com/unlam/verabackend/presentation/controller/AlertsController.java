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
        log.info("REST Request: GET - Consultando bitácora de alertas para el cuidador: [{}] con filtros activos", user.getEmail());

        Page<Alerts> alertsPage = manageAlertsUseCase.getAlertsHistory(user.getEmail(), resolved, riskLevel, search, page);

        return ResponseEntity.ok(convertToPagedAlertsResponse(alertsPage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertsDetailResponse> getAlertDetail(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        log.info("REST Request: GET - Solicitando informe técnico de la alerta ID [{}] por el operador [{}]", id, user.getEmail());

        Alerts alert = manageAlertsUseCase.getAlertDetail(id, user.getEmail());
        return ResponseEntity.ok(AlertsDetailResponse.fromDomain(alert));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlert(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        log.info("REST Request: DELETE - Solicitando descarte definitivo de la alerta ID [{}] por el operador [{}]", id, user.getEmail());

        manageAlertsUseCase.deleteAlert(id, user.getEmail());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<Void> resolveAlert(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        log.info("REST Request: PATCH - Solicitando cierre y resolución de la alerta ID [{}] por el operador [{}]", id, user.getEmail());

        manageAlertsUseCase.resolveAlert(id, user.getEmail());
        return ResponseEntity.noContent().build();
    }

    private PagedResponse<AlertsResponse> convertToPagedAlertsResponse(Page<Alerts> alertsPage) {
        log.debug("REST Response: Convirtiendo {} alertas encontradas hacia el DTO adaptado de la UI.", alertsPage.getNumberOfElements());
        return PagedResponse.fromPage(alertsPage, AlertsResponse::fromDomain);
    }
}