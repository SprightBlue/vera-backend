package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.domain.model.Message;
import com.unlam.verabackend.domain.model.MessageSource;
import com.unlam.verabackend.domain.ports.in.AnalyzeMessageUseCase;
import com.unlam.verabackend.presentation.dto.MessagePresentation;
import com.unlam.verabackend.presentation.dto.AnalysisPresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/analysis")
public class AnalyzeController {

    private final AnalyzeMessageUseCase analyzeMessageUseCase;

    public AnalyzeController(AnalyzeMessageUseCase analyzeMessageUseCase) {
        this.analyzeMessageUseCase = analyzeMessageUseCase;
    }

    @PostMapping
    public ResponseEntity<?> analyze(@RequestBody MessagePresentation request) {
        try {
            if (request == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("El cuerpo de la solicitud no puede estar vacío");
            }

            Message message = toDomain(request);
            Analysis analysis = analyzeMessageUseCase.analyzeMessage(message);

            return ResponseEntity.ok(toDto(analysis));

        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ocurrió un error interno al procesar el análisis: " + ex.getMessage());
        }
    }

    private Message toDomain(MessagePresentation dto) {
        return new Message(
                UUID.randomUUID(),
                dto.userId(),
                dto.content(),
                MessageSource.fromString(dto.source()),
                LocalDateTime.now()
        );
    }

    private AnalysisPresentation toDto(Analysis analysis) {
        return new AnalysisPresentation(
                analysis.getId(),
                analysis.getMessageId(),
                analysis.getRiskLevel().getDisplayName(),
                analysis.getSuspiciousPatterns(),
                analysis.getRecommendation(),
                analysis.getCreatedAt()
        );
    }
}
