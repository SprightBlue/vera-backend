package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.domain.model.RiskLevel;
import com.unlam.verabackend.domain.port.in.AnalyzeContentUseCase;
import com.unlam.verabackend.domain.port.in.ChatUseCase;
import com.unlam.verabackend.domain.port.in.ManageAnalysisUseCase;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.presentation.dto.PagedResponse;
import com.unlam.verabackend.presentation.dto.AnalysisDetailResponse;
import com.unlam.verabackend.presentation.dto.AnalysisResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalyzeContentUseCase analyzeContentUseCase;
    private final ManageAnalysisUseCase manageAnalysisUseCase;
    private final ChatUseCase chatUseCase;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<AnalysisDetailResponse> analyze(
            @AuthenticationPrincipal User user,
            @RequestParam(value = "text", required = false) String text,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "source") String source
    ) {
        log.info("REST Request: POST - Solicitando análisis de contenido por [{}] desde el origen [{}]", user.getEmail(), source);
        validateAnalysisPayload(text, file);

        var result = analyzeContentUseCase.execute(user.getEmail(), text, file, source);

        log.debug("REST Response: Análisis procesado exitosamente. Empaquetando resultado detallado.");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AnalysisDetailResponse.fromDomain(result));
    }

    @PostMapping("/chat/{analysisId}")
    public ResponseEntity<UUID> initializeChatFromAnalysis(
            @AuthenticationPrincipal User user,
            @PathVariable UUID analysisId
    ) {
        log.info("REST Request: POST - Usuario [{}] solicita derivación a chat contextual desde el análisis ID [{}]", user.getEmail(), analysisId);

        UUID newChatId = chatUseCase.createChat(user.getEmail(), analysisId);

        return ResponseEntity.status(HttpStatus.CREATED).body(newChatId);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<AnalysisResponse>> getAnalysisHistory(
            @AuthenticationPrincipal User user,
            @RequestParam(value = "riskLevel", required = false) RiskLevel riskLevel,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page
    ) {
        log.info("REST Request: GET - Solicitando historial de análisis filtrado para [{}]", user.getEmail());

        Page<Analysis> historyPage = manageAnalysisUseCase.getAnalysisHistory(user.getEmail(), riskLevel, search, page);

        return ResponseEntity.ok(convertToPagedAnalysisResponse(historyPage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnalysisDetailResponse> getAnalysisDetail(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id
    ) {
        log.info("REST Request: GET - Consultando desglose del análisis ID [{}] por el operador [{}]", id, user.getEmail());

        Analysis analysis = manageAnalysisUseCase.getAnalysisDetail(id, user.getEmail());
        return ResponseEntity.ok(AnalysisDetailResponse.fromDomain(analysis));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnalysis(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        log.info("REST Request: DELETE - Solicitando eliminación del análisis ID [{}] por el operador [{}]", id, user.getEmail());

        manageAnalysisUseCase.deleteAnalysis(id, user.getEmail());
        return ResponseEntity.noContent().build();
    }

    private void validateAnalysisPayload(String text, MultipartFile file) {
        if ((text == null || text.isBlank()) && (file == null || file.isEmpty())) {
            log.warn("Payload Validation Exception: Estructura vacía. Se requiere texto o archivo adjunto válidos.");
            throw new IllegalArgumentException("Debe proporcionar al menos un texto o un archivo para analizar.");
        }
    }

    private PagedResponse<AnalysisResponse> convertToPagedAnalysisResponse(Page<Analysis> historyPage) {
        log.debug("REST Response: Formateando {} registros de análisis históricos a estructura paginada.", historyPage.getNumberOfElements());
        return PagedResponse.fromPage(historyPage, AnalysisResponse::fromDomain);
    }
}