package com.unlam.verabackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GenerateInvitationResponse {
    private String token;
    private String invitationLink;
}