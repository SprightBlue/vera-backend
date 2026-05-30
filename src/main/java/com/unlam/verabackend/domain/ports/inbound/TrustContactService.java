package com.unlam.verabackend.domain.ports.inbound;

import java.util.List;

import com.unlam.verabackend.presentation.dto.request.GenerateInvitationRequest;
import com.unlam.verabackend.presentation.dto.response.GenerateInvitationResponse;
import com.unlam.verabackend.presentation.dto.response.InvitationDetailsResponse;
import com.unlam.verabackend.presentation.dto.response.ProtectedPersonResponse;

public interface TrustContactService {
    
    List<ProtectedPersonResponse> getMyProtectedPeople(String carerEmail);

    GenerateInvitationResponse generateInvitationLink(GenerateInvitationRequest request, String carerEmail);

    InvitationDetailsResponse getInvitationDetails(String token);

    void acceptInvitation(String token, String protectedUserEmail);
}
