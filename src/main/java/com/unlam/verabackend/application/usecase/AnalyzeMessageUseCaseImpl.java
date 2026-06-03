package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.model.*;
import com.unlam.verabackend.domain.ports.in.AnalyzeMessageUseCase;
import com.unlam.verabackend.domain.ports.out.*;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.presentation.controller.RiskAlertController;
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
    private final com.unlam.verabackend.domain.repository.UserRepository userRepository;

    public AnalyzeMessageUseCaseImpl(AnalysisRepository analysisRepository,
                                     RiskAlertRepository riskAlertRepository,
                                     UserCaregiverRepository userCaregiverRepository,
                                     SafeBrowsingApiPort safeBrowsingApiPort,
                                     GeminiApiPort geminiApiPort,
                                     com.unlam.verabackend.domain.repository.UserRepository userRepository) {
        this.analysisRepository = analysisRepository;
        this.riskAlertRepository = riskAlertRepository;
        this.userCaregiverRepository = userCaregiverRepository;
        this.safeBrowsingApiPort = safeBrowsingApiPort;
        this.geminiApiPort = geminiApiPort;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Analysis analyzeMessage(String userEmail, String content, MessageSource source) {
        User dbUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el email: " + userEmail));

        DomainUser domainUser = new DomainUser();
        domainUser.setId(dbUser.getId());
        domainUser.setEmail(dbUser.getEmail());

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
            RiskAlert savedAlert = riskAlertRepository.save(alert);

            Long caregiverId = relation.getCaregiver().getId();

            RiskAlertController.sendNotificationToCaregiver(caregiverId, savedAlert);
        }
    }
}
