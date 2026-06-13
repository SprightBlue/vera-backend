package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.domain.model.RiskLevel;
import com.unlam.verabackend.domain.port.in.AnalyzeContentUseCase;
import com.unlam.verabackend.domain.port.in.ManageAnalysisUseCase;
import com.unlam.verabackend.presentation.dto.PagedResponse;
import com.unlam.verabackend.presentation.dto.AnalysisDetailResponse;
import com.unlam.verabackend.presentation.dto.AnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalyzeContentUseCase analyzeContentUseCase;
    private final ManageAnalysisUseCase manageAnalysisUseCase;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<AnalysisDetailResponse> analyze(
            // 🚀 PROD: @AuthenticationPrincipal User user,
            @RequestHeader("user-email") String email,
            @RequestParam(value = "text", required = false) String text,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "source") String source
    ) {
        // 🚀 PROD: String email = user.getEmail();

        Analysis result = analyzeContentUseCase.execute(email, text, file, source);
        return new ResponseEntity<>(AnalysisDetailResponse.fromDomain(result), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<AnalysisResponse>> getHistoryByUserEmail(
            // 🚀 PROD: @AuthenticationPrincipal User user,
            @RequestHeader("user-email") String email,
            @RequestParam(value = "riskLevel", required = false) String riskLevel,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        // 🚀 PROD: String email = user.getEmail();

        Page<Analysis> historyPage;
        if (riskLevel != null && !riskLevel.isBlank()) {
            historyPage = manageAnalysisUseCase.getHistoryByUserEmailAndRiskLevel(email, RiskLevel.valueOf(riskLevel.toUpperCase().strip()), pageable);
        } else {
            historyPage = manageAnalysisUseCase.getHistoryByUserEmail(email, pageable);
        }

        PagedResponse<AnalysisResponse> response = PagedResponse.fromPage(
                historyPage,
                AnalysisResponse::fromDomain
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnalysisDetailResponse> getAnalysisDetail(
            // 🚀 PROD: @AuthenticationPrincipal User user,
            @RequestHeader("user-email") String email,
            @PathVariable UUID id
    ) {
        // 🚀 PROD: String email = user.getEmail();

        Analysis analysis = manageAnalysisUseCase.getAnalysisDetail(id, email);
        return ResponseEntity.ok(AnalysisDetailResponse.fromDomain(analysis));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnalysis(
            // 🚀 PROD: @AuthenticationPrincipal User user,
            @RequestHeader("user-email") String email,
            @PathVariable UUID id
    ) {
        // 🚀 PROD: String email = user.getEmail();

        manageAnalysisUseCase.deleteAnalysis(id, email);
        return ResponseEntity.noContent().build();
    }
}