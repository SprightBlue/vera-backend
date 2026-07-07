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
    private final SseService sseService;

    @Override
    @Transactional
    public Analysis execute(String userEmail, String rawText, MultipartFile file, String sourceStr) {
        log.info("Iniciando flujo de análisis de contenido solicitado por el usuario: {}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("Fallo de análisis: Usuario no registrado con el email: {}", userEmail);
                    return new ResourceNotFoundException("Usuario no encontrado: " + userEmail);
                });

        Source source = parseSource(sourceStr);
        ContentProcessResult content = processContent(rawText, file);

        log.info("Ejecutando chequeo preventivo de URLs extraídas. Cantidad detectada: {}", content.urls().size());
        List<String> safeBrowsingReport = checkUrlProvider.checkUrls(content.urls());

        String prompt = promptBuilder.buildPrompt(safeBrowsingReport, rawText, content.fileText(), source);

        log.info("Enviando payload al proveedor de IA para análisis heurístico de fraude.");
        AiResult aiResult = aiProvider.analyzeContent(prompt, content.fileToAnalyze());
        if (aiResult == null) {
            log.error("Fallo crítico del motor de IA: El proveedor devolvió una respuesta nula.");
            throw new IllegalStateException("Error crítico: Respuesta nula del motor de IA.");
        }

        return saveAnalysisAndHandleAlerts(aiResult, user, source);
    }

    private Analysis saveAnalysisAndHandleAlerts(AiResult aiResult, User user, Source source) {
        log.info("Persistiendo resultados del análisis heurístico en base de datos.");

        Analysis analysis = analysisRepository.save(buildAnalysis(aiResult, user, source));
        log.info("Análisis guardado exitosamente. ID asignado: {} | Nivel de Riesgo: {}", analysis.getId(), analysis.getRiskLevel());

        handleRiskAlerts(analysis, user);

        return analysis;
    }

    private Source parseSource(String sourceStr) {
        if (sourceStr == null || sourceStr.isBlank()) {
            log.warn("Fallo de validación: El origen de los datos (source) es obligatorio.");
            throw new IllegalArgumentException("Origen (source) obligatorio.");
        }
        return Source.valueOf(sourceStr.toUpperCase().strip());
    }

    private ContentProcessResult processContent(String rawText, MultipartFile file) {
        List<String> urls = new ArrayList<>();
        if (rawText != null && !rawText.isBlank()) {
            urls.addAll(urlExtractor.findUrls(rawText));
        }

        String fileText = "";
        MultipartFile fileForAi = null;

        if (file != null && !file.isEmpty()) {
            String filename = file.getOriginalFilename();
            log.debug("Procesando archivo adjunto recibido: {}", filename);
            fileValidator.validate(file);

            if (fileValidator.isDocument(filename)) {
                fileText = urlExtractor.convertDocumentToText(file);
                urls.addAll(urlExtractor.findUrls(fileText));
            } else if (fileValidator.isMultimedia(filename)) {
                fileForAi = file;
            }
        }
        return new ContentProcessResult(urls, fileText, fileForAi);
    }

    private void handleRiskAlerts(Analysis analysis, User user) {
        if (analysis.getRiskLevel() == null) return;

        if (user.getRole() == Role.CARER) {
            log.info("Análisis realizado por un cuidador. Se omite el envío de alertas de riesgo.");
            return;
        }

        List<TrustContact> contacts = trustContactRepository.findByProtectedUserId(user.getId());
        log.debug("Evaluando alertas de riesgo para {} contactos de confianza registrados.", contacts.size());

        contacts.stream()
                .filter(contact -> shouldNotify(contact.getSensitivityLevel(), analysis.getRiskLevel()))
                .forEach(contact -> triggerAlert(analysis, user, contact));
    }

    private boolean shouldNotify(SensitivityLevel sensitivity, RiskLevel risk) {
        return switch (sensitivity) {
            case ALTO -> true;
            case MEDIO -> risk == RiskLevel.MEDIUM || risk == RiskLevel.HIGH;
            case BAJO -> risk == RiskLevel.HIGH;
        };
    }

    private void triggerAlert(Analysis analysis, User user, TrustContact contact) {
        Alerts alert = alertsRepository.save(buildAlert(analysis), contact.getId());
        log.info("Contacto de confianza notificado. Alerta ID: {} enviada al cuidador: {}", alert.getId(), contact.getCarer().getEmail());

        sseService.createAndSendNotification(
                contact.getCarer(),
                NotificationsType.ALERT,
                user.getFullName(),
                Map.of("alertId", alert.getId().toString(), "riskLevel", alert.getRiskLevel().toString())
        );
    }

    private Analysis buildAnalysis(AiResult aiResult, User user, Source source) {
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

    private Alerts buildAlert(Analysis analysis) {
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