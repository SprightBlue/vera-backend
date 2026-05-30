package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.presentation.dto.request.GenerateInvitationRequest;
import com.unlam.verabackend.presentation.dto.response.GenerateInvitationResponse;
import com.unlam.verabackend.presentation.dto.response.InvitationDetailsResponse;
import com.unlam.verabackend.presentation.dto.response.ProtectedPersonResponse;
import com.unlam.verabackend.domain.ports.inbound.TrustContactService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trust")
@RequiredArgsConstructor
public class TrustContactController {

    private final TrustContactService trustContactService;

    @GetMapping("/protected")
    public ResponseEntity<List<ProtectedPersonResponse>> getMyProtectedPeople(
            Authentication authentication) {
        
        List<ProtectedPersonResponse> protectedPeople = trustContactService.getMyProtectedPeople(authentication.getName());
        return ResponseEntity.ok(protectedPeople);
    }

    @PostMapping("/invite")
    public ResponseEntity<GenerateInvitationResponse> generateInvitation(
            @Valid @RequestBody GenerateInvitationRequest request, 
            Authentication authentication) {
        
        GenerateInvitationResponse response = trustContactService.generateInvitationLink(request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/invite/{token}")
    public ResponseEntity<InvitationDetailsResponse> getInvitationDetails(@PathVariable String token) {
        InvitationDetailsResponse response = trustContactService.getInvitationDetails(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/invite/{token}/accept")
    public ResponseEntity<String> acceptInvitation(
            @PathVariable String token, 
            Authentication authentication) {
        
        trustContactService.acceptInvitation(token, authentication.getName());
        
        return ResponseEntity.ok("¡Invitación aceptada exitosamente! Ahora estás protegido.");
    }
}
