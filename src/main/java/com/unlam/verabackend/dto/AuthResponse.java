package com.unlam.verabackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {

    private String token;      
    private String email;
    private String fullName;
    private String role;
}