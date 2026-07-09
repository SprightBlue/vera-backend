package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.model.DashboardData;
import com.unlam.verabackend.domain.model.Role;
import com.unlam.verabackend.domain.port.in.GetDashboardDataUseCase;
import com.unlam.verabackend.presentation.dto.DashboardResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final GetDashboardDataUseCase getDashboardDataUseCase;

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(Authentication authentication) {
        String email = authentication.getName();
        Role role = extractDomainRole(authentication);

        log.info("Presentation Controller: Solicitud de Dashboard entrante para [{}] con rol [{}]", email, role);

        DashboardData domainData = getDashboardDataUseCase.execute(email, role);

        DashboardResponse response = DashboardResponse.fromDomain(domainData, role);

        return ResponseEntity.ok(response);
    }

    private Role extractDomainRole(Authentication authentication) {
        try {
            String roleStr = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse("PROTECTED")
                    .replace("ROLE_", "");

            return Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Presentation Error: La autoridad de seguridad no coincide con ningún Role de dominio. Aplicando fallback PROTECTED.");
            return Role.PROTECTED;
        }
    }
}