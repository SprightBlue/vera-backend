package com.unlam.verabackend.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.unlam.verabackend.domain.port.in.TrustContactUseCase;
import com.unlam.verabackend.presentation.dto.CarerResponse;
import com.unlam.verabackend.presentation.dto.GenerateInvitationRequest;
import com.unlam.verabackend.presentation.dto.GenerateInvitationResponse;
import com.unlam.verabackend.presentation.dto.InvitationDetailsResponse;
import com.unlam.verabackend.presentation.dto.ProtectedPersonResponse;
import com.unlam.verabackend.infrastructure.entity.User;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/trust")
@RequiredArgsConstructor
public class TrustContactController {

    private final TrustContactUseCase trustContactUseCase;

    @GetMapping("/protected-people")
    public ResponseEntity<List<ProtectedPersonResponse>> getMyProtectedPeople(@AuthenticationPrincipal User user) {
        List<ProtectedPersonResponse> protectedPeople = trustContactUseCase.getMyProtectedPeople(user.getEmail());
        return ResponseEntity.ok(protectedPeople);
    }

    @PostMapping("/invite")
    public ResponseEntity<GenerateInvitationResponse> generateInvitation(
            @Valid @RequestBody GenerateInvitationRequest request,
            @AuthenticationPrincipal User user) {
        GenerateInvitationResponse response = trustContactUseCase.generateInvitationLink(request, user.getEmail());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/invite/{token}")
    public ResponseEntity<InvitationDetailsResponse> getInvitationDetails(@PathVariable String token) {
        InvitationDetailsResponse response = trustContactUseCase.getInvitationDetails(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/invite/{token}/accept")
    public ResponseEntity<String> acceptInvitation(@PathVariable String token, @AuthenticationPrincipal User user) {
        trustContactUseCase.acceptInvitation(token, user.getEmail());
        return ResponseEntity.ok("¡Invitación aceptada exitosamente! Ahora estás protegido.");
    }

    @GetMapping("/protected-people/{id}")
    public ResponseEntity<ProtectedPersonResponse> getProtectedPersonById(@PathVariable Long id) {
        ProtectedPersonResponse protectedPerson = trustContactUseCase.getProtectedPersonById(id);
        return ResponseEntity.ok(protectedPerson);
    }

    @DeleteMapping("/protected-people/{id}")
    public ResponseEntity<Void> deleteProtectedPerson(@PathVariable Long id) {
        trustContactUseCase.deleteProtectedPerson(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/protected-people/edit-person/{id}")
    public ResponseEntity<ProtectedPersonResponse> updateInformation(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        String fullName = (String) payload.get("fullName");
        String relationship = (String) payload.get("relationship");
        String contactNumber = (String) payload.get("contactNumber");
        String image = (String) payload.get("image");

        ProtectedPersonResponse protectedPerson = trustContactUseCase.updateInformation(id, fullName, relationship, contactNumber, image);

        return ResponseEntity.ok(protectedPerson);
    }

    @PatchMapping("/protected-people/{id}")
    public ResponseEntity<Void> updatePreferences(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        String sensitivityLevel = (String) payload.get("sensitivityLevel");
        Boolean notifyHighRisk = (Boolean) payload.get("notifyHighRisk");
        Boolean receiveAlertSummaries = (Boolean) payload.get("receiveAlertSummaries"); 

        trustContactUseCase.updateConfiguration(id, sensitivityLevel, notifyHighRisk);
        
        return ResponseEntity.noContent().build();
    }

    // --- ENDPOINTS PROTEGIDOS POR AUTH PARA LA BANDEJA DE NOTIFICACIONES ---

    @GetMapping("/invitations/pending")
    public ResponseEntity<List<InvitationDetailsResponse>> getMyPendingInvitations(@AuthenticationPrincipal User user) {
        List<InvitationDetailsResponse> pending = trustContactUseCase.getPendingInvitationsForMe(user.getEmail());
        return ResponseEntity.ok(pending);
    }

    @PostMapping("/invitations/{id}/accept")
    public ResponseEntity<Map<String, String>> acceptInvitationById(@PathVariable Long id, @AuthenticationPrincipal User user) {
        trustContactUseCase.acceptInvitationById(id, user.getEmail());
        return ResponseEntity.ok(Map.of("message", "Invitación aceptada exitosamente."));
    }

    @PostMapping("/invitations/{id}/reject")
    public ResponseEntity<Map<String, String>> rejectInvitationById(@PathVariable Long id, @AuthenticationPrincipal User user) {
        trustContactUseCase.rejectInvitationById(id, user.getEmail());
        return ResponseEntity.ok(Map.of("message", "Invitación rechazada exitosamente."));
    }

    @GetMapping("/my-carers")
    public ResponseEntity<List<CarerResponse>> getMyCarers(@AuthenticationPrincipal User user) {
        List<CarerResponse> carers = trustContactUseCase.getMyCarers(user.getEmail());
        return ResponseEntity.ok(carers);
    }
}