//package com.unlam.verabackend.presentation.controller;
//
//import com.unlam.verabackend.domain.port.in.ManageRiskAlertUseCase;
//import com.unlam.verabackend.infrastructure.entity.User;
//import com.unlam.verabackend.application.service.NotificationSseService;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/v1/risk-alerts")
//public class RiskAlertController {
//
//    private final ManageRiskAlertUseCase manageRiskAlertUseCase;
//    private final NotificationSseService notificationSseService;
//
//    public RiskAlertController(ManageRiskAlertUseCase manageRiskAlertUseCase, NotificationSseService notificationSseService) {
//        this.manageRiskAlertUseCase = manageRiskAlertUseCase;
//        this.notificationSseService = notificationSseService;
//    }
//
//    @GetMapping("/active")
//    public ResponseEntity<List<RiskAlertResponse>> getActiveAlerts(@AuthenticationPrincipal User user) {
//        List<RiskAlert> alerts = manageRiskAlertUseCase.getActiveAlertsByCarerEmail(user.getEmail());
//        List<RiskAlertResponse> response = alerts.stream().map(this::mapToResponse).toList();
//        return ResponseEntity.ok(response);
//    }
//
//    @GetMapping("/{alertId}")
//    public ResponseEntity<RiskAlertResponse> getAlertById(@PathVariable String alertId) {
//        RiskAlert alert = manageRiskAlertUseCase.getAlertById(alertId);
//        return ResponseEntity.ok(mapToResponse(alert));
//    }
//
//    @PostMapping("/{alertId}/solve")
//    public ResponseEntity<Void> solveAlert(@PathVariable String alertId) {
//        manageRiskAlertUseCase.markAlertAsSolved(alertId);
//        return ResponseEntity.noContent().build();
//    }
//
//    // Canal unificado de Streaming asincrónico por Spring Security
//    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public SseEmitter streamAlerts(@AuthenticationPrincipal User user) {
//        return notificationSseService.createEmitter(user.getId());
//    }
//
//    public void sendNotificationToTrustContact(Long carerId, RiskAlert alert) {
//        RiskAlertResponse dto = mapToResponse(alert);
//        notificationSseService.sendNotification(carerId, "RISK_ALERT", dto);
//    }
//
//    private RiskAlertResponse mapToResponse(RiskAlert alert) {
//        return new RiskAlertResponse(
//                alert.getId().toString(),
//                alert.getAnalysis().getUser().getFullName(),
//                alert.getAnalysis().getUser().getEmail(),
//                alert.getAnalysis().getContentSummary(),
//                alert.getAnalysis().getSource().name(),
//                alert.getAnalysis().getRiskLevel().name(),
//                alert.getAnalysis().getSuspiciousPatterns(),
//                alert.getCreatedAt()
//        );
//    }
//
//    public record RiskAlertResponse(
//            String alertId,
//            String protectedUserName,
//            String protectedUserEmail,
//            String messageContent,
//            String source,
//            String riskLevel,
//            String suspiciousPatterns,
//            LocalDateTime createdAt
//    ) {}
//}