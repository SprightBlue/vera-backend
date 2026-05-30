package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.ports.inbound.ManageRiskAlertUseCase;
import com.unlam.verabackend.domain.model.RiskAlert;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/risk-alerts")
public class RiskAlertController {

    private final ManageRiskAlertUseCase manageRiskAlertUseCase;

    public RiskAlertController(ManageRiskAlertUseCase manageRiskAlertUseCase) {
        this.manageRiskAlertUseCase = manageRiskAlertUseCase;
    }

    @GetMapping("/caregiver/{caregiverId}/active")
    public ResponseEntity<List<RiskAlertResponse>> getActiveAlerts(@PathVariable Long caregiverId) {
        List<RiskAlert> alerts = manageRiskAlertUseCase.getActiveAlertsByCaregiver(caregiverId);

        List<RiskAlertResponse> response = alerts.stream()
                .map(alert -> new RiskAlertResponse(
                        alert.getId().toString(),
                        alert.getAnalysis().getUser().getFullName(),
                        alert.getAnalysis().getContent(),
                        alert.getAnalysis().getMessageSource().getDisplayName(),
                        alert.getAnalysis().getRiskLevel().name(),
                        alert.getAnalysis().getSuspiciousPatterns(),
                        alert.getCreatedAt()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{alertId}/solve")
    public ResponseEntity<Void> solveAlert(@PathVariable String alertId) {
        manageRiskAlertUseCase.markAlertAsSolved(alertId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{alertId}/contact-link")
    public ResponseEntity<ContactLinkResponse> getContactLink(@PathVariable String alertId) {
        String mailtoLink = manageRiskAlertUseCase.getContactLinkForUser(alertId);
        return ResponseEntity.ok(new ContactLinkResponse(mailtoLink));
    }

    public record RiskAlertResponse(
            String alertId,
            String protectedUserName,
            String messageContent,
            String source,
            String riskLevel,
            String suspiciousPatterns,
            LocalDateTime createdAt
    ) {}

    public record ContactLinkResponse(String link) {}
}
