package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.ports.in.ManageRiskAlertUseCase;
import com.unlam.verabackend.domain.model.RiskAlert;
import com.unlam.verabackend.infrastructure.entity.User;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/risk-alerts")
public class RiskAlertController {

    private final ManageRiskAlertUseCase manageRiskAlertUseCase;

    // 👈 Renombrado semánticamente para tus contactos de confianza (Carers)
    private static final Map<Long, SseEmitter> trustContactsEmitters = new ConcurrentHashMap<>();

    public RiskAlertController(ManageRiskAlertUseCase manageRiskAlertUseCase) {
        this.manageRiskAlertUseCase = manageRiskAlertUseCase;
    }

    @GetMapping("/active")
    public ResponseEntity<List<RiskAlertResponse>> getActiveAlerts(@AuthenticationPrincipal User user) {
        // 👈 Cambiado para usar el nuevo método de búsqueda por email del Carer
        List<RiskAlert> alerts = manageRiskAlertUseCase.getActiveAlertsByCarerEmail(user.getEmail());
        List<RiskAlertResponse> response = alerts.stream().map(this::mapToResponse).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{alertId}")
    public ResponseEntity<RiskAlertResponse> getAlertById(@PathVariable String alertId) {
        RiskAlert alert = manageRiskAlertUseCase.getAlertById(alertId);
        return ResponseEntity.ok(mapToResponse(alert));
    }

    @PostMapping("/{alertId}/solve")
    public ResponseEntity<Void> solveAlert(@PathVariable String alertId) {
        manageRiskAlertUseCase.markAlertAsSolved(alertId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAlerts(@AuthenticationPrincipal User user) {
        SseEmitter emitter = new SseEmitter(10 * 60 * 1000L);
        Long carerId = user.getId(); // 👈 ID del contacto de confianza (Carer)

        trustContactsEmitters.put(carerId, emitter);

        emitter.onCompletion(() -> trustContactsEmitters.remove(carerId));
        emitter.onTimeout(() -> trustContactsEmitters.remove(carerId));
        emitter.onError((e) -> trustContactsEmitters.remove(carerId));

        try {
            emitter.send(SseEmitter.event().name("INIT").data("Conectado"));
        } catch (IOException ignored) {}

        return emitter;
    }

    // 👈 Método estático renombrado para acoplarse al refactor de TrustContact
    public static void sendNotificationToTrustContact(Long carerId, RiskAlert alert) {
        SseEmitter emitter = trustContactsEmitters.get(carerId);
        if (emitter != null) {
            try {
                // Reutilizamos el método de mapeo para evitar código duplicado
                RiskAlertResponse dto = mapToResponseStatic(alert);
                emitter.send(SseEmitter.event().name("RISK_ALERT").data(dto));
            } catch (IOException e) {
                trustContactsEmitters.remove(carerId);
            }
        }
    }

    private RiskAlertResponse mapToResponse(RiskAlert alert) {
        return mapToResponseStatic(alert);
    }

    // Método estático auxiliar para permitir el mapeo tanto en contextos de instancia como estáticos
    private static RiskAlertResponse mapToResponseStatic(RiskAlert alert) {
        return new RiskAlertResponse(
                alert.getId().toString(),
                alert.getAnalysis().getUser().getFullName(),
                alert.getAnalysis().getUser().getEmail(),
                alert.getAnalysis().getContent(),
                alert.getAnalysis().getMessageSource().getDisplayName(),
                alert.getAnalysis().getRiskLevel().name(),
                alert.getAnalysis().getSuspiciousPatterns(),
                alert.getCreatedAt()
        );
    }

    public record RiskAlertResponse(
            String alertId,
            String protectedUserName,
            String protectedUserEmail,
            String messageContent,
            String source,
            String riskLevel,
            String suspiciousPatterns,
            LocalDateTime createdAt
    ) {}
}