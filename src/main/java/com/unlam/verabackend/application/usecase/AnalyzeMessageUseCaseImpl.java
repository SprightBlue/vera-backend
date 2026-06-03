package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.model.*;
import com.unlam.verabackend.domain.ports.in.AnalyzeMessageUseCase;
import com.unlam.verabackend.domain.ports.out.*;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.entity.TrustContact; // 👈 Cambiado a tu nueva entidad
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository; // 👈 Cambiado al nuevo repositorio
import com.unlam.verabackend.presentation.controller.RiskAlertController;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AnalyzeMessageUseCaseImpl implements AnalyzeMessageUseCase {

    private final AnalysisRepository analysisRepository;
    private final RiskAlertRepository riskAlertRepository;
    private final TrustContactRepository trustContactRepository; // 👈 Cambiado
    private final SafeBrowsingApiPort safeBrowsingApiPort;
    private final GeminiApiPort geminiApiPort;
    private final com.unlam.verabackend.domain.repository.UserRepository userRepository;

    public AnalyzeMessageUseCaseImpl(AnalysisRepository analysisRepository,
                                     RiskAlertRepository riskAlertRepository,
                                     TrustContactRepository trustContactRepository, // 👈 Cambiado
                                     SafeBrowsingApiPort safeBrowsingApiPort,
                                     GeminiApiPort geminiApiPort,
                                     com.unlam.verabackend.domain.repository.UserRepository userRepository) {
        this.analysisRepository = analysisRepository;
        this.riskAlertRepository = riskAlertRepository;
        this.trustContactRepository = trustContactRepository; // 👈 Cambiado
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

        // 👈 VALIDACIÓN: Solo despacha alertas si es HIGH y evita explícitamente el estado UNDEFINED
        if (RiskLevel.HIGH.equals(analysis.getRiskLevel()) && !RiskLevel.UNDEFINED.equals(analysis.getRiskLevel())) {
            dispatchAlertsToTrustContacts(analysis); // 👈 Nombre de método actualizado
        }

        return analysis;
    }

    private void dispatchAlertsToTrustContacts(Analysis analysis) {
        List<TrustContact> contacts = trustContactRepository.findByProtectedUserId(analysis.getUser().getId());

        for (TrustContact contact : contacts) {
            if (contact.isNotifyHighRisk()) {

                // 1. Extraemos la entidad JPA del Carer
                User jpaCarer = contact.getCarer();

                // 2. La mapeamos a tu objeto de dominio DomainUser
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

                // 3. Ahora sí, pasamos el objeto de dominio correcto 🎉
                RiskAlert alert = RiskAlert.createActive(analysis, domainCarer);
                RiskAlert savedAlert = riskAlertRepository.save(alert);

                Long carerId = jpaCarer.getId();
                RiskAlertController.sendNotificationToTrustContact(carerId, savedAlert);
            }
        }
    }
}