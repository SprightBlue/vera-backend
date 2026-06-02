package com.unlam.verabackend.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ProtectedPersonResponse {
    
    private Long id;
    private String fullName;
    private String email;
    private String contactNumber;
    private String relationship;
    private String status;
}
