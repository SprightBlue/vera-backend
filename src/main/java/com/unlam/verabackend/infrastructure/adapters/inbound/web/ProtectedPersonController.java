package com.unlam.verabackend.infrastructure.adapters.inbound.web;

import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.domain.ports.in.CreateProtectedPersonUseCase;
import com.unlam.verabackend.infrastructure.dto.CreateProtectedPersonRequest;
import com.unlam.verabackend.infrastructure.dto.ProtectedPersonResponse;
import com.unlam.verabackend.infrastructure.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/protected-persons")
@CrossOrigin
public class ProtectedPersonController {

    private final CreateProtectedPersonUseCase createProtectedPersonUseCase;

    private final UserRepository userRepository;

    public ProtectedPersonController(
            CreateProtectedPersonUseCase createProtectedPersonUseCase,
            UserRepository userRepository
    ) {
        this.createProtectedPersonUseCase = createProtectedPersonUseCase;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<Void> createProtectedPerson(
            @RequestBody CreateProtectedPersonRequest request,
            Authentication authentication
    ) {

        String email = authentication.getName();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow();

        createProtectedPersonUseCase.execute(
                user.getId(),
                request
        );

        return ResponseEntity.ok().build();

    }

    @GetMapping
    public ResponseEntity<List<ProtectedPersonResponse>> getProtectedPersons(
            Authentication authentication
    ) {

        String email = authentication.getName();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow();

        List<ProtectedPersonResponse> response =
                createProtectedPersonUseCase
                        .getByUserId(user.getId())
                        .stream()
                        .map(person -> new ProtectedPersonResponse(
                                person.getId(),
                                person.getFullName(),
                                person.getRelationshipType().name(),
                                person.getPhone(),
                                person.getEmail(),
                                person.getHighRiskAlertsEnabled(),
                                person.getWeeklySummaryEnabled(),
                                person.getNotificationSensitivity()
                        ))
                        .toList();

        return ResponseEntity.ok(response);

    }

}