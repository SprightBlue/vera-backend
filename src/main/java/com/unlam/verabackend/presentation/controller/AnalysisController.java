package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.ports.in.AnalyzeMessageUseCase;
import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.domain.model.MessageSource;
import com.unlam.verabackend.infrastructure.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisController {

    private final AnalyzeMessageUseCase analyzeMessageUseCase;

    public AnalysisController(AnalyzeMessageUseCase analyzeMessageUseCase) {
        this.analyzeMessageUseCase = analyzeMessageUseCase;
    }

    @PostMapping("/message")
    public ResponseEntity<AnalysisResponse> analyzeMessage(
            @RequestBody AnalysisRequest request,
            @AuthenticationPrincipal User user) {

        String userEmail = user.getEmail();
        MessageSource source = MessageSource.fromString(request.source());

        Analysis result = analyzeMessageUseCase.analyzeMessage(userEmail, request.content(), source);

        AnalysisResponse response = new AnalysisResponse(
                result.getId().toString(),
                result.getRiskLevel().name(),
                result.getSuspiciousPatterns(),
                result.getRecommendation(),
                result.getCreatedAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public record AnalysisRequest(String content, String source) {}

    public record AnalysisResponse(String id, String riskLevel, String suspiciousPatterns, String recommendation, LocalDateTime createdAt) {}
}
