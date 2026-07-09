package com.unlam.verabackend.domain.port.in;

import java.io.IOException;
import java.util.List;

import com.unlam.verabackend.presentation.dto.*;
import org.springframework.web.multipart.MultipartFile;

public interface TrustContactUseCase {
    
    List<ProtectedPersonResponse> getMyProtectedPeople(String carerEmail);

    GenerateInvitationResponse generateInvitationLink(GenerateInvitationRequest request, String carerEmail);

    InvitationDetailsResponse getInvitationDetails(String token);

    void acceptInvitation(String token, String protectedUserEmail);

    void deleteProtectedPerson(Long id, String status, String carerEmail);

    void updateConfiguration(Long id, String sensitivityLevelStr, Boolean notifyHighRisk);

    List<InvitationDetailsResponse> getPendingInvitationsForMe(String myEmail);

    void acceptInvitationById(Long invitationId, String protectedUserEmail);
    
    void rejectInvitationById(Long invitationId, String protectedUserEmail);

    List<CarerResponse> getMyCarers(String protectedUserEmail);

    ProtectedPersonResponse getProtectedPersonById(Long id, String status, String carerEmail);

    String uploadProtectedPersonImage(MultipartFile image) throws IOException;

    ProtectedPersonResponse updateInformation(Long id, String fullName, String relationship, String contactNumber, String image);

}