package com.unlam.verabackend.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProtectedPersonResponse {
    
    private Long id;
    private String fullName;
    private String email;
}
