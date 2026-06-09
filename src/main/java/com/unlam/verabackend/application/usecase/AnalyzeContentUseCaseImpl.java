package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.application.service.ValidatorService;
import com.unlam.verabackend.application.service.PromptBuilderService;
import com.unlam.verabackend.application.service.ExtractorService;
import com.unlam.verabackend.application.service.DomainCreationService;
import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.domain.model.Alerts;
import com.unlam.verabackend.domain.model.Source;
import com.unlam.verabackend.domain.model.RiskLevel;
import com.unlam.verabackend.domain.port.in.AnalyzeContentUseCase;
import com.unlam.verabackend.domain.port.out.AnalysisRepository;
import com.unlam.verabackend.domain.port.out.AlertsRepository;
import com.unlam.verabackend.domain.port.out.GeminiProvider;
import com.unlam.verabackend.domain.port.out.GeminiResult;
import com.unlam.verabackend.domain.port.out.SafeBrowsingProvider;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnalyzeContentUseCaseImpl implements AnalyzeContentUseCase {

    private final ValidatorService fileValidator;
    private final ExtractorService urlExtractor;
    private final SafeBrowsingProvider safeBrowsingProvider;
    private final PromptBuilderService promptBuilder;
    private final GeminiProvider geminiProvider;
    private final UserRepository userRepository;
    private final AnalysisRepository analysisRepository;
    private final TrustContactRepository trustContactRepository;
    private final AlertsRepository alertsRepository;
    private final DomainCreationService domainCreationService;

    public AnalyzeContentUseCaseImpl(ValidatorService fileValidator, ExtractorService urlExtractor,
                                     SafeBrowsingProvider safeBrowsingProvider,
                                     PromptBuilderService promptBuilder,
                                     GeminiProvider geminiProvider,
                                     UserRepository userRepository,
                                     AnalysisRepository analysisRepository,
                                     TrustContactRepository trustContactRepository,
                                     AlertsRepository alertsRepository,
                                     DomainCreationService domainCreationService) {
        this.fileValidator = fileValidator;
        this.urlExtractor = urlExtractor;
        this.safeBrowsingProvider = safeBrowsingProvider;
        this.promptBuilder = promptBuilder;
        this.geminiProvider = geminiProvider;
        this.userRepository = userRepository;
        this.analysisRepository = analysisRepository;
        this.trustContactRepository = trustContactRepository;
        this.alertsRepository = alertsRepository;
        this.domainCreationService = domainCreationService;
    }

    @Override
    @Transactional
    public Analysis execute(String userEmail, String rawText, MultipartFile file, String sourceStr) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + userEmail));

        boolean hasText = rawText != null && !rawText.isBlank();
        boolean hasFile = file != null && !file.isEmpty();

        if (!hasText && !hasFile) {
            throw new IllegalArgumentException("Debe ingresar un texto o adjuntar un archivo para comenzar el análisis.");
        }

        if (sourceStr == null || sourceStr.isBlank()) {
            throw new IllegalArgumentException("El origen de la consulta (source) es obligatorio.");
        }

        Source source = Source.valueOf(sourceStr.toUpperCase().strip());

        List<String> allUrls = new ArrayList<>();
        if (hasText) {
            allUrls.addAll(urlExtractor.findUrls(rawText));
        }

        String fileText = "";
        MultipartFile fileToSendToGemini = null;

        if (hasFile) {
            fileValidator.validate(file);
            String filename = file.getOriginalFilename();

            if (fileValidator.isDocument(filename)) {
                fileText = urlExtractor.convertDocumentToText(file);
                if (fileText != null && !fileText.isBlank()) {
                    allUrls.addAll(urlExtractor.findUrls(fileText));
                }
            } else if (fileValidator.isMultimedia(filename)) {
                fileToSendToGemini = file;
            }
        }

        List<String> safeBrowsingReport = safeBrowsingProvider.checkUrls(allUrls);
        String prompt = promptBuilder.buildPrompt(safeBrowsingReport, rawText, fileText, source);

        GeminiResult aiResult = geminiProvider.analyzeContent(prompt, fileToSendToGemini);
        if (aiResult == null) {
            throw new IllegalStateException("No se pudo obtener una respuesta válida del motor de análisis inteligente.");
        }

        Analysis analysis = domainCreationService.buildAnalysis(aiResult, user, source);

        Analysis savedAnalysis = analysisRepository.save(analysis);

        if (savedAnalysis.getRiskLevel() != null && RiskLevel.HIGH.equals(savedAnalysis.getRiskLevel())) {

            List<TrustContact> activeCarers = trustContactRepository.findByProtectedUserId(user.getId());

            for (TrustContact contact : activeCarers) {
                Alerts newAlert = domainCreationService.buildAlert(savedAnalysis);

                alertsRepository.save(newAlert, contact.getId());
            }
        }

        return savedAnalysis;
    }
}