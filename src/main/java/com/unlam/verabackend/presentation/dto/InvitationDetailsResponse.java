package com.unlam.verabackend.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InvitationDetailsResponse {
    private String protectedFullName; 
    private String carerFullName;     
    private String relationship;      
}