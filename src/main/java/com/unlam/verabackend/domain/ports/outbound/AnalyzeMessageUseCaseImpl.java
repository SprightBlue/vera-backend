package com.unlam.verabackend.domain.ports.outbound;

import com.unlam.verabackend.domain.model.*;
import com.unlam.verabackend.domain.ports.inbound.AnalyzeMessageUseCase;
import com.unlam.verabackend.domain.ports.out.*;
import com.unlam.verabackend.infrastructure.repository.AnalysisRepository;
import com.unlam.verabackend.infrastructure.repository.RiskAlertRepository;
import com.unlam.verabackend.infrastructure.repository.UserCaregiverRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AnalyzeMessageUseCaseImpl implements AnalyzeMessageUseCase {

    private final AnalysisRepository analysisRepository;
    private final RiskAlertRepository riskAlertRepository;
    private final UserCaregiverRepository userCaregiverRepository;
    private final SafeBrowsingApiPort safeBrowsingApiPort;
    private final GeminiApiPort geminiApiPort;

    public AnalyzeMessageUseCaseImpl(AnalysisRepository analysisRepository,
                                     RiskAlertRepository riskAlertRepository,
                                     UserCaregiverRepository userCaregiverRepository,
                                     SafeBrowsingApiPort safeBrowsingApiPort,
                                     GeminiApiPort geminiApiPort) {
        this.analysisRepository = analysisRepository;
        this.riskAlertRepository = riskAlertRepository;
        this.userCaregiverRepository = userCaregiverRepository;
        this.safeBrowsingApiPort = safeBrowsingApiPort;
        this.geminiApiPort = geminiApiPort;
    }

    @Override
    @Transactional
    public Analysis analyzeMessage(DomainUser domainUser, String content, MessageSource source) {
        if (content == null || content.isBlank()) throw new IllegalArgumentException("El contenido no puede estar vacío");
        if (domainUser == null || domainUser.getId() == null) throw new IllegalArgumentException("Usuario de dominio requerido");

        UrlValidation urlValidation = safeBrowsingApiPort.checkUrlsInContent(content);

        MessageAssessment messageAssessment = geminiApiPort.analyzeMessageContent(content, urlValidation);

        Analysis analysis = Analysis.create(
                domainUser, content, source,
                messageAssessment.riskLevel(), messageAssessment.suspiciousPatterns(), messageAssessment.recommendation()
        );
        analysisRepository.save(analysis);

        if (RiskLevel.HIGH.equals(analysis.getRiskLevel())) {
            dispatchAlertsToCaregivers(analysis);
        }

        return analysis;
    }

    private void dispatchAlertsToCaregivers(Analysis analysis) {
        List<UserCaregiver> relations = userCaregiverRepository.findByUserId(analysis.getUser().getId());
        for (UserCaregiver relation : relations) {
            RiskAlert alert = RiskAlert.createActive(analysis, relation.getCaregiver());
            riskAlertRepository.save(alert);
        }
    }
}
