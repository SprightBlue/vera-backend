package com.unlam.verabackend.domain.port.in;

import java.util.List;

import com.unlam.verabackend.presentation.dto.CarerResponse;
import com.unlam.verabackend.presentation.dto.GenerateInvitationRequest;
import com.unlam.verabackend.presentation.dto.GenerateInvitationResponse;
import com.unlam.verabackend.presentation.dto.InvitationDetailsResponse;
import com.unlam.verabackend.presentation.dto.ProtectedPersonResponse;

public interface TrustContactUseCase {
    
    List<ProtectedPersonResponse> getMyProtectedPeople(String carerEmail);

    GenerateInvitationResponse generateInvitationLink(GenerateInvitationRequest request, String carerEmail);

    InvitationDetailsResponse getInvitationDetails(String token);

    void acceptInvitation(String token, String protectedUserEmail);

    void deleteProtectedPerson(Long id);

    void updateConfiguration(Long id, String sensitivityLevelStr, Boolean notifyHighRisk);

    List<InvitationDetailsResponse> getPendingInvitationsForMe(String myEmail);
    void acceptInvitationById(Long invitationId, String protectedUserEmail);
    void rejectInvitationById(Long invitationId, String protectedUserEmail);

    List<CarerResponse> getMyCarers(String protectedUserEmail);
}