package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.model.AlertDetail;
import com.unlam.verabackend.domain.ports.in.GetAlertDetailUseCase;
import com.unlam.verabackend.domain.ports.in.ManageRiskAlertUseCase;
import com.unlam.verabackend.domain.model.RiskAlert;
import com.unlam.verabackend.presentation.mapper.AlertPresentationMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/risk-alerts")
public class RiskAlertController {

    private final ManageRiskAlertUseCase manageRiskAlertUseCase;
    private final GetAlertDetailUseCase getAlertDetailUseCase;

    public RiskAlertController(ManageRiskAlertUseCase manageRiskAlertUseCase, GetAlertDetailUseCase getAlertDetailUseCase) {
        this.manageRiskAlertUseCase = manageRiskAlertUseCase;
        this.getAlertDetailUseCase = getAlertDetailUseCase;
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


    @GetMapping("/{alertId}")
    public ResponseEntity<?> getDetail(@PathVariable UUID alertId, @AuthenticationPrincipal(expression = "id") Long requestingUserId) throws AccessDeniedException {
        if (requestingUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AlertDetail detail = getAlertDetailUseCase.getDetail(alertId, requestingUserId);
        return ResponseEntity.ok(AlertPresentationMapper.toDetailPresentation(detail));
    }
}
