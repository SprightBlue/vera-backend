package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.application.service.*;
import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.*;
import com.unlam.verabackend.domain.port.in.AnalyzeContentUseCase;
import com.unlam.verabackend.domain.port.out.*;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyzeContentUseCaseImpl implements AnalyzeContentUseCase {

    private final ValidatorService fileValidator;
    private final ExtractorService urlExtractor;
    private final CheckUrlProvider checkUrlProvider;
    private final PromptBuilderService promptBuilder;
    private final AiProvider aiProvider;
    private final UserRepository userRepository;
    private final AnalysisRepository analysisRepository;
    private final AlertsRepository alertsRepository;
    private final TrustContactRepository trustContactRepository;
    private final NotificationService notificationService;
    private final RtcProvider rtcProvider;

    @Override
    @Transactional
    public Analysis execute(String userEmail, String rawText, MultipartFile file, String sourceStr) {
        log.info("UseCase: Iniciando pipeline heurístico de análisis de fraude solicitado por [{}]", userEmail);

        User user = fetchUserByEmail(userEmail);
        Source source = sanitizeAndParseSource(sourceStr);

        ContentProcessResult content = extractAndProcessContent(rawText, file);
        List<String> safeBrowsingReport = checkUrlProvider.checkUrls(content.urls());

        String finalPrompt = promptBuilder.buildPrompt(safeBrowsingReport, rawText, content.fileText(), source);
        AiResult aiResult = requestAiInference(finalPrompt, content.fileToAnalyze());

        return saveAnalysisAndEvaluateRisks(aiResult, user, source);
    }

    private User fetchUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("UseCase Error: Denegado. El emisor [{}] no figura en el sistema.", email);
                    return new ResourceNotFoundException("Usuario no encontrado: " + email);
                });
    }

    private Source sanitizeAndParseSource(String sourceStr) {
        if (sourceStr == null || sourceStr.isBlank()) {
            log.warn("UseCase Validation: Se intentó analizar contenido sin especificar canal de origen.");
            throw new IllegalArgumentException("El origen de los datos (source) es obligatorio.");
        }
        return Source.valueOf(sourceStr.toUpperCase().strip());
    }

    private ContentProcessResult extractAndProcessContent(String rawText, MultipartFile file) {
        List<String> urls = new ArrayList<>();

        if (rawText != null && !rawText.isBlank()) {
            urls.addAll(urlExtractor.findUrls(rawText));
        }

        String fileText = "";
        MultipartFile fileForAi = null;

        if (file != null && !file.isEmpty()) {
            String filename = file.getOriginalFilename();
            log.debug("UseCase: Analizando metadatos del adjunto [{}]", filename);
            fileValidator.validate(file);

            if (fileValidator.isDocument(filename)) {
                fileText = urlExtractor.convertDocumentToText(file);
                urls.addAll(urlExtractor.findUrls(fileText));
            } else if (fileValidator.isMultimedia(filename)) {
                fileForAi = file;
            }
        }

        log.debug("UseCase: Pre-procesamiento listo. Extrayendo [{}] URLs potenciales para SafeBrowsing.", urls.size());
        return new ContentProcessResult(urls, fileText, fileForAi);
    }

    private AiResult requestAiInference(String prompt, MultipartFile multimediaFile) {
        log.info("UseCase: Transmitiendo contexto estructurado al motor de IA...");
        AiResult aiResult = aiProvider.analyzeContent(prompt, multimediaFile);

        if (aiResult == null) {
            log.error("UseCase Error: El proveedor de IA retornó un payload nulo o corrupto.");
            throw new IllegalStateException("Error crítico: Respuesta vacía del motor de IA.");
        }
        return aiResult;
    }

    private Analysis saveAnalysisAndEvaluateRisks(AiResult aiResult, User user, Source source) {
        Analysis analysis = buildAnalysisEntity(aiResult, user, source);
        Analysis savedAnalysis = analysisRepository.save(analysis);

        log.info("UseCase: Registro de análisis ID [{}] guardado con nivel de riesgo [{}]",
                savedAnalysis.getId(), savedAnalysis.getRiskLevel());

        processSecurityAlerts(savedAnalysis, user);
        return savedAnalysis;
    }

    private void processSecurityAlerts(Analysis analysis, User user) {
        if (analysis.getRiskLevel() == null) return;

        if (Role.CARER.equals(user.getRole())) {
            log.info("UseCase: Análisis ejecutado preventivamente por un cuidador. Se suprime el árbol de alertas.");
            return;
        }

        List<TrustContact> contacts = trustContactRepository.findByProtectedUserId(user.getId());
        log.debug("UseCase: Evaluando matriz de sensibilidad para {} contactos del protegido [{}].", contacts.size(), user.getEmail());

        contacts.stream()
                .filter(contact -> isAlertRequired(contact.getSensitivityLevel(), analysis.getRiskLevel()))
                .forEach(contact -> dispatchAlert(analysis, user, contact));
    }

    private boolean isAlertRequired(SensitivityLevel sensitivity, RiskLevel risk) {
        return switch (sensitivity) {
            case ALTO -> true;
            case MEDIO -> risk == RiskLevel.MEDIUM || risk == RiskLevel.HIGH;
            case BAJO -> risk == RiskLevel.HIGH;
        };
    }

    private void dispatchAlert(Analysis analysis, User protectedUser, TrustContact contact) {
        Alerts alert = buildAlertEntity(analysis);
        Alerts savedAlert = alertsRepository.save(alert, contact.getId());

        log.info("UseCase: Disparando alerta ID [{}] hacia el tutor asignado [{}]", savedAlert.getId(), contact.getCarer().getEmail());

        Map<String, Object> notificationPayload = Map.of(
                "alertId", savedAlert.getId().toString(),
                "riskLevel", savedAlert.getRiskLevel().toString()
        );

        notificationService.createAndDispatch(
                contact.getCarer(),
                NotificationsType.ALERT,
                protectedUser.getFullName(),
                notificationPayload
        );

        if (contact.getCarer() != null && contact.getCarer().getEmail() != null) {
            String carerEmail = contact.getCarer().getEmail();
            log.info("UseCase: Sincronizando nueva alerta en el Dashboard del Carer [{}] vía RTC", carerEmail);
            rtcProvider.publishCarerDashboardAlertUpdate(carerEmail, savedAlert);
        }
    }

    private Analysis buildAnalysisEntity(AiResult aiResult, User user, Source source) {
        return Analysis.builder()
                .title(aiResult.title())
                .contentSummary(aiResult.contentSummary())
                .riskLevel(RiskLevel.valueOf(aiResult.riskLevel().toUpperCase().strip()))
                .riskType(RiskType.valueOf(aiResult.riskType().toUpperCase().strip()))
                .riskPercentage(aiResult.riskPercentage())
                .suspiciousPatterns(aiResult.suspiciousPatterns())
                .recommendation(aiResult.recommendation())
                .user(user)
                .createdAt(LocalDateTime.now())
                .source(source)
                .build();
    }

    private Alerts buildAlertEntity(Analysis analysis) {
        return Alerts.builder()
                .title(analysis.getTitle())
                .source(analysis.getSource())
                .contentSummary(analysis.getContentSummary())
                .riskLevel(analysis.getRiskLevel())
                .riskType(analysis.getRiskType())
                .riskPercentage(analysis.getRiskPercentage())
                .suspiciousPatterns(analysis.getSuspiciousPatterns())
                .isResolved(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private record ContentProcessResult(List<String> urls, String fileText, MultipartFile fileToAnalyze) {}
}