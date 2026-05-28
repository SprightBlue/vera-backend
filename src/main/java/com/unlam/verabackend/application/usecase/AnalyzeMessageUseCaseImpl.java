package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.application.helper.AnalysisHelper;
import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.domain.model.Message;
import com.unlam.verabackend.domain.model.RiskAlert;
import com.unlam.verabackend.domain.model.RiskLevel;
import com.unlam.verabackend.domain.model.UserCaregiver;
import com.unlam.verabackend.domain.ports.in.AnalyzeMessageUseCase;
import com.unlam.verabackend.domain.ports.out.AnalysisRepositoryPort;
import com.unlam.verabackend.domain.ports.out.GeminiApiPort;
import com.unlam.verabackend.domain.ports.out.MessageRepositoryPort;
import com.unlam.verabackend.domain.ports.out.RiskAlertRepositoryPort;
import com.unlam.verabackend.domain.ports.out.SafeBrowsingApiPort;
import com.unlam.verabackend.domain.ports.out.UserCaregiverRepositoryPort;
import com.unlam.verabackend.infrastructure.dto.GeminiDto;
import com.unlam.verabackend.infrastructure.dto.SafeBrowsingDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnalyzeMessageUseCaseImpl implements AnalyzeMessageUseCase {

    private final MessageRepositoryPort messageRepositoryPort;
    private final AnalysisRepositoryPort analysisRepositoryPort;
    private final SafeBrowsingApiPort safeBrowsingApiPort;
    private final GeminiApiPort geminiApiPort;
    private final AnalysisHelper analysisHelper;
    private final UserCaregiverRepositoryPort userCaregiverRepositoryPort;
    private final RiskAlertRepositoryPort riskAlertRepositoryPort;

    public AnalyzeMessageUseCaseImpl(MessageRepositoryPort messageRepositoryPort,
                                     AnalysisRepositoryPort analysisRepositoryPort,
                                     SafeBrowsingApiPort safeBrowsingApiPort,
                                     GeminiApiPort geminiApiPort,
                                     AnalysisHelper analysisHelper,
                                     UserCaregiverRepositoryPort userCaregiverRepositoryPort,
                                     RiskAlertRepositoryPort riskAlertRepositoryPort) {
        this.messageRepositoryPort = messageRepositoryPort;
        this.analysisRepositoryPort = analysisRepositoryPort;
        this.safeBrowsingApiPort = safeBrowsingApiPort;
        this.geminiApiPort = geminiApiPort;
        this.analysisHelper = analysisHelper;
        this.userCaregiverRepositoryPort = userCaregiverRepositoryPort;
        this.riskAlertRepositoryPort = riskAlertRepositoryPort;
    }

    @Override
    @Transactional
    public Analysis analyzeMessage(Message message) {
        if (message == null || message.getContent() == null || message.getContent().isBlank()) {
            throw new IllegalArgumentException("El mensaje a analizar no puede ser nulo");
        }

        messageRepositoryPort.save(message);

        List<String> urls = analysisHelper.extractAllUrls(message.getContent());
        SafeBrowsingDto safeBrowsingDto = safeBrowsingApiPort.checkUrls(urls);

        String prompt = analysisHelper.buildPrompt(message.getContent(), urls, safeBrowsingDto);
        GeminiDto geminiDto = geminiApiPort.analyzeMessage(prompt);

        Analysis analysis = analysisHelper.buildAnalysis(message, geminiDto);
        analysisRepositoryPort.save(analysis);

        if (RiskLevel.HIGH.equals(analysis.getRiskLevel())) {
            List<UserCaregiver> caregivers = userCaregiverRepositoryPort.findByUserId(message.getUserId());

            for (UserCaregiver caregiver : caregivers) {
                RiskAlert alert = analysisHelper.buildAlert(analysis.getId(), caregiver.getCaregiverId());
                riskAlertRepositoryPort.save(alert);
            }
        }

        return analysis;
    }
}
