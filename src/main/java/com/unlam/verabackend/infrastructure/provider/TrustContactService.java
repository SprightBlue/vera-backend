package com.unlam.verabackend.services.Interface;

import java.util.List;

import com.unlam.verabackend.dto.GenerateInvitationRequest;
import com.unlam.verabackend.dto.GenerateInvitationResponse;
import com.unlam.verabackend.dto.InvitationDetailsResponse;
import com.unlam.verabackend.dto.ProtectedPersonResponse;

public interface TrustContactService {
    
    List<ProtectedPersonResponse> getMyProtectedPeople(String carerEmail);

    GenerateInvitationResponse generateInvitationLink(GenerateInvitationRequest request, String carerEmail);

    InvitationDetailsResponse getInvitationDetails(String token);

    void acceptInvitation(String token, String protectedUserEmail);
}
