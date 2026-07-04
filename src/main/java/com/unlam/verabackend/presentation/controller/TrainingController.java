package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.port.in.ManageTrainingUseCase;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.presentation.dto.PagedResponse;
import com.unlam.verabackend.presentation.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/training")
@RequiredArgsConstructor
public class TrainingController {

    private final ManageTrainingUseCase manageTrainingUseCase;

    @GetMapping("/scenarios")
    public ResponseEntity<List<TrainingScenarioResponse>> getScenarios(@AuthenticationPrincipal User user) {
        var scenarios = manageTrainingUseCase.getAvailableScenarios(user.getEmail())
                .stream().map(TrainingScenarioResponse::fromDomain).toList();
        return ResponseEntity.ok(scenarios);
    }

    @GetMapping("/scenarios/{scenarioId}")
    public ResponseEntity<TrainingScenarioResponse> getScenario(@AuthenticationPrincipal User user, @PathVariable UUID scenarioId) {
        return ResponseEntity.ok(TrainingScenarioResponse.fromDomain(manageTrainingUseCase.getScenarioById(scenarioId)));
    }

    @PostMapping("/scenarios/{scenarioId}/submit")
    public ResponseEntity<TrainingSessionResponse> submitAnswer(@AuthenticationPrincipal User user, @PathVariable UUID scenarioId, @RequestBody SubmitAnswerRequest request) {
        var session = manageTrainingUseCase.submitAnswer(user.getEmail(), scenarioId, request.selectedOptionId());
        return ResponseEntity.ok(TrainingSessionResponse.fromDomain(session));
    }

    // Cuidador
    @PostMapping("/protected-person/{trustContactId}/assign")
    public ResponseEntity<Void> assignScenario(@AuthenticationPrincipal User user, @PathVariable Long trustContactId, @RequestBody AssignTrainingRequest request) {
        manageTrainingUseCase.assignScenario(user.getEmail(), trustContactId, request.scenarioId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/protected-person/{trustContactId}/progress")
    public ResponseEntity<TrainingProgressResponse> getProgress(@AuthenticationPrincipal User user, @PathVariable Long trustContactId) {
        return ResponseEntity.ok(manageTrainingUseCase.getProgressForProtected(user.getEmail(), trustContactId));
    }

    @GetMapping("/protected-person/{trustContactId}/stats")
    public ResponseEntity<TrainingStatsResponse> getStats(@AuthenticationPrincipal User user, @PathVariable Long trustContactId) {
        return ResponseEntity.ok(manageTrainingUseCase.getStatsForProtected(user.getEmail(), trustContactId));
    }

    @GetMapping("/protected-person/{trustContactId}/sessions")
    public ResponseEntity<PagedResponse<TrainingSessionResponse>> getSessions(@AuthenticationPrincipal User user, @PathVariable Long trustContactId, @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        var page = manageTrainingUseCase.getSessionsForProtected(user.getEmail(), trustContactId, pageable)
                .map(TrainingSessionResponse::fromDomain);
        return ResponseEntity.ok(PagedResponse.fromPage(page, r -> r));
    }
}