package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.model.*;
import com.unlam.verabackend.domain.ports.in.AnalyzeMessageUseCase;
import com.unlam.verabackend.domain.ports.out.*;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.entity.TrustContact;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import com.unlam.verabackend.application.service.NotificationSseService; // 👈 Importamos el servicio de tiempo real unificado
import com.unlam.verabackend.presentation.controller.RiskAlertController.RiskAlertResponse; // 👈 Importamos el DTO de respuesta si es necesario, o lo mapeamos acá
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AnalyzeMessageUseCaseImpl implements AnalyzeMessageUseCase {

    private final AnalysisRepository analysisRepository;
    private final RiskAlertRepository riskAlertRepository;
    private final TrustContactRepository trustContactRepository;
    private final SafeBrowsingApiPort safeBrowsingApiPort;
    private final GeminiApiPort geminiApiPort;
    private final com.unlam.verabackend.domain.repository.UserRepository userRepository;
    private final NotificationSseService notificationSseService; // 👈 Inyectamos el servicio unificado de SSE

    public AnalyzeMessageUseCaseImpl(AnalysisRepository analysisRepository,
                                     RiskAlertRepository riskAlertRepository,
                                     TrustContactRepository trustContactRepository,
                                     SafeBrowsingApiPort safeBrowsingApiPort,
                                     GeminiApiPort geminiApiPort,
                                     com.unlam.verabackend.domain.repository.UserRepository userRepository,
                                     NotificationSseService notificationSseService) { // 👈 Agregado al constructor
        this.analysisRepository = analysisRepository;
        this.riskAlertRepository = riskAlertRepository;
        this.trustContactRepository = trustContactRepository;
        this.safeBrowsingApiPort = safeBrowsingApiPort;
        this.geminiApiPort = geminiApiPort;
        this.userRepository = userRepository;
        this.notificationSseService = notificationSseService;
    }

    @Override
    @Transactional
    public Analysis analyzeMessage(String userEmail, String content, MessageSource source) {
        User dbUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario encontrado con el email: " + userEmail));

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

        if (RiskLevel.HIGH.equals(analysis.getRiskLevel()) && !RiskLevel.UNDEFINED.equals(analysis.getRiskLevel())) {
            dispatchAlertsToTrustContacts(analysis);
        }

        return analysis;
    }

    private void dispatchAlertsToTrustContacts(Analysis analysis) {
        List<TrustContact> contacts = trustContactRepository.findByProtectedUserId(analysis.getUser().getId());

        for (TrustContact contact : contacts) {
            if (contact.isNotifyHighRisk()) {

                User jpaCarer = contact.getCarer();

                DomainUser domainCarer = new DomainUser(
                        jpaCarer.getId(),
                        jpaCarer.getFullName(),
                        jpaCarer.getEmail(),
                        jpaCarer.getRole() != null ? Role.valueOf(jpaCarer.getRole().name()) : null,
                        jpaCarer.getCreatedAt(),
                        jpaCarer.getUpdatedAt(),
                        jpaCarer.isAccountNonLocked(),
                        jpaCarer.isEnabled()
                );

                RiskAlert alert = RiskAlert.createActive(analysis, domainCarer);
                RiskAlert savedAlert = riskAlertRepository.save(alert);

                Long carerId = jpaCarer.getId();

                // 🌟 CORRECCIÓN TIEMPO REAL: Mapeamos la alerta al DTO que espera recibir el Front
                RiskAlertResponse realtimeDto = new RiskAlertResponse(
                        savedAlert.getId().toString(),
                        savedAlert.getAnalysis().getUser().getFullName(),
                        savedAlert.getAnalysis().getUser().getEmail(),
                        savedAlert.getAnalysis().getContent(),
                        savedAlert.getAnalysis().getMessageSource().getDisplayName(),
                        savedAlert.getAnalysis().getRiskLevel().name(),
                        savedAlert.getAnalysis().getSuspiciousPatterns(),
                        savedAlert.getCreatedAt()
                );

                // 🔥 Despachamos usando el canal SSE unificado con el nombre de evento correcto "RISK_ALERT"
                notificationSseService.sendNotification(carerId, "RISK_ALERT", realtimeDto);
            }
        }
    }
}