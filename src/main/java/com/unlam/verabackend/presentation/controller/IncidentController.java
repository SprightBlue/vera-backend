package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.model.IncidentStepKey;
import com.unlam.verabackend.domain.port.in.ManageIncidentsUseCase;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.presentation.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final ManageIncidentsUseCase manageIncidentsUseCase;

    @PostMapping
    public ResponseEntity<IncidentDetailResponse> createIncident(@AuthenticationPrincipal User user, @RequestBody CreateIncidentRequest request) {
        var incident = manageIncidentsUseCase.createIncident(
                user.getEmail(),
                request.actionType(),
                request.sharedDataTypes(),
                request.description()
        );
        return new ResponseEntity<>(IncidentDetailResponse.fromDomain(incident), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<IncidentSummaryResponse>> getMyIncidents(@AuthenticationPrincipal User user, @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<IncidentSummaryResponse> page = manageIncidentsUseCase
                .getMyIncidents(user.getEmail(), pageable)
                .map(IncidentSummaryResponse::fromDomain);

        return ResponseEntity.ok(PagedResponse.fromPage(page, r -> r));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidentDetailResponse> getIncidentDetail(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        var incident = manageIncidentsUseCase.getIncidentDetail(id, user.getEmail());
        return ResponseEntity.ok(IncidentDetailResponse.fromDomain(incident));
    }

    @PatchMapping("/{id}/steps/{stepKey}/complete")
    public ResponseEntity<IncidentDetailResponse> completeStep(@AuthenticationPrincipal User user, @PathVariable UUID id, @PathVariable IncidentStepKey stepKey) {
        var incident = manageIncidentsUseCase.completeStep(id, stepKey, user.getEmail());
        return ResponseEntity.ok(IncidentDetailResponse.fromDomain(incident));
    }

    @GetMapping("/protected-person/{trustContactId}")
    public ResponseEntity<PagedResponse<IncidentSummaryResponse>> getIncidentsByTrustContact(@AuthenticationPrincipal User user, @PathVariable Long trustContactId, @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<IncidentSummaryResponse> page = manageIncidentsUseCase
                .getIncidentsByTrustContact(trustContactId, user.getEmail(), pageable)
                .map(IncidentSummaryResponse::fromDomain);

        return ResponseEntity.ok(PagedResponse.fromPage(page, r -> r));
    }
}