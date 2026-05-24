package com.unlam.verabackend.analysis.application.usecase;

import com.unlam.verabackend.analysis.application.service.AnalysisBusinessRulesService;
import com.unlam.verabackend.analysis.domain.model.Analysis;
import com.unlam.verabackend.analysis.domain.model.Message;
import com.unlam.verabackend.analysis.domain.model.MessageSource;
import com.unlam.verabackend.analysis.domain.model.RiskLevel;
import com.unlam.verabackend.analysis.domain.ports.in.AnalyzeTextUseCase;
import com.unlam.verabackend.analysis.domain.ports.out.AnalysisRepositoryPort;
import com.unlam.verabackend.analysis.domain.ports.out.GeminiAnalysisPort;
import com.unlam.verabackend.analysis.domain.ports.out.MessageRepositoryPort;
import com.unlam.verabackend.analysis.domain.ports.out.SafeBrowsingPort;
import com.unlam.verabackend.analysis.infrastructure.dto.GeminiAnalysisRequest;
import com.unlam.verabackend.analysis.infrastructure.dto.GeminiAnalysisResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AnalyzeTextUseCaseImpl implements AnalyzeTextUseCase {

    private final MessageRepositoryPort messageRepositoryPort;
    private final AnalysisRepositoryPort analysisRepositoryPort;
    private final SafeBrowsingPort safeBrowsingPort;
    private final GeminiAnalysisPort geminiAnalysisPort;
    private final AnalysisBusinessRulesService businessRulesService;

    public AnalyzeTextUseCaseImpl(MessageRepositoryPort messageRepositoryPort,
                                  AnalysisRepositoryPort analysisRepositoryPort,
                                  SafeBrowsingPort safeBrowsingPort,
                                  GeminiAnalysisPort geminiAnalysisPort,
                                  AnalysisBusinessRulesService businessRulesService) {
        this.messageRepositoryPort = messageRepositoryPort;
        this.analysisRepositoryPort = analysisRepositoryPort;
        this.safeBrowsingPort = safeBrowsingPort;
        this.geminiAnalysisPort = geminiAnalysisPort;
        this.businessRulesService = businessRulesService;
    }

    @Override
    public Analysis analyzeMessage(Message message) {
        businessRulesService.validateMessageContent(message.getContent());

        if (message.getSource() == null) {
            message.setSource(MessageSource.UNKNOWN);
        }

        messageRepositoryPort.save(message);

        Optional<String> url = businessRulesService.extractFirstUrl(message.getContent());
        boolean hasUrl = url.isPresent();
        boolean maliciousUrlDetected = url.map(safeBrowsingPort::checkUrl).orElse(false);

        GeminiAnalysisResponse geminiResponse = geminiAnalysisPort.analyzeMessage(
                new GeminiAnalysisRequest(message.getContent(), hasUrl, maliciousUrlDetected)
        );

        RiskLevel finalRiskLevel = businessRulesService.resolveRiskLevel(
                geminiResponse.getRiskLevel(),
                maliciousUrlDetected
        );

        boolean finalThreat = businessRulesService.resolveThreat(
                geminiResponse.isThreat(),
                maliciousUrlDetected
        );

        Analysis analysis = new Analysis(
                UUID.randomUUID(),
                message.getId(),
                finalThreat,
                finalRiskLevel,
                geminiResponse.getSuspiciousPatterns(),
                geminiResponse.getRecommendation(),
                LocalDateTime.now()
        );

        analysisRepositoryPort.save(analysis);
        return analysis;
    }
}
