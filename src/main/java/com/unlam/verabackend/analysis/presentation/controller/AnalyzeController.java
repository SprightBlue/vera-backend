package com.unlam.verabackend.analysis.presentation.controller;

import com.unlam.verabackend.analysis.domain.model.Analysis;
import com.unlam.verabackend.analysis.domain.model.Message;
import com.unlam.verabackend.analysis.domain.model.MessageSource;
import com.unlam.verabackend.analysis.domain.ports.in.AnalyzeTextUseCase;
import com.unlam.verabackend.analysis.presentation.dto.AnalyzeRequestDto;
import com.unlam.verabackend.analysis.presentation.dto.AnalysisResultDto;
import com.unlam.verabackend.analysis.presentation.dto.ErrorDto;
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

    private final AnalyzeTextUseCase analyzeTextUseCase;

    public AnalyzeController(AnalyzeTextUseCase analyzeTextUseCase) {
        this.analyzeTextUseCase = analyzeTextUseCase;
    }

    @PostMapping
    public ResponseEntity<?> analyze(@RequestBody AnalyzeRequestDto request) {
        try {
            Message message = toDomain(request);
            Analysis analysis = analyzeTextUseCase.analyzeMessage(message);
            return ResponseEntity.ok(toDto(analysis));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDto(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorDto(ex.getMessage()));
        }
    }

    private Message toDomain(AnalyzeRequestDto dto) {
        UUID id = dto.getId() != null ? dto.getId() : UUID.randomUUID();
        MessageSource source = parseSource(dto.getSource());
        LocalDateTime receivedAt = LocalDateTime.now();
        return new Message(id, dto.getUserId(), dto.getContent(), source, receivedAt);
    }

    private MessageSource parseSource(String source) {
        if (source == null || source.isBlank()) return MessageSource.UNKNOWN;
        try {
            return MessageSource.valueOf(source);
        } catch (IllegalArgumentException ex) {
            return MessageSource.UNKNOWN;
        }
    }

    private AnalysisResultDto toDto(Analysis analysis) {
        AnalysisResultDto dto = new AnalysisResultDto();
        dto.setId(analysis.getId());
        dto.setMessageId(analysis.getMessageId());
        dto.setThreat(analysis.isThreat());
        dto.setRiskLevel(analysis.getRiskLevel() != null ? analysis.getRiskLevel().name() : null);
        dto.setSuspiciousPatterns(analysis.getSuspiciousPatterns());
        dto.setRecommendation(analysis.getRecommendation());
        dto.setCreatedAt(analysis.getCreatedAt());
        return dto;
    }
}
