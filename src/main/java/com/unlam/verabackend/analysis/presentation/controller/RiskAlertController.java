package com.unlam.verabackend.analysis.presentation.controller;

import com.unlam.verabackend.analysis.domain.model.RiskAlert;
import com.unlam.verabackend.analysis.domain.ports.in.MarkAlertAsReceivedUseCase;
import com.unlam.verabackend.analysis.presentation.dto.RiskAlertPresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/alerts")
public class RiskAlertController {

    private final MarkAlertAsReceivedUseCase markAlertAsReceivedUseCase;

    public RiskAlertController(MarkAlertAsReceivedUseCase markAlertAsReceivedUseCase) {
        this.markAlertAsReceivedUseCase = markAlertAsReceivedUseCase;
    }

    @PostMapping("/{alertId}/received")
    public ResponseEntity<?> received(@PathVariable UUID alertId) {
        try {
            if (alertId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("El ID de la alerta no puede ser nulo");
            }

            RiskAlert alert = markAlertAsReceivedUseCase.markAsReceived(alertId);

            return ResponseEntity.ok(toResponse(alert));

        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ex.getMessage());

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al actualizar el estado de la alerta: " + ex.getMessage());
        }
    }

    private RiskAlertPresentation toResponse(RiskAlert alert) {
        return new RiskAlertPresentation(
                alert.getId(),
                alert.getAnalysisId(),
                alert.getCaregiverId(),
                alert.isReceived(),
                alert.getCreatedAt()
        );
    }
}
