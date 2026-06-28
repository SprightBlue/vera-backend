package com.unlam.verabackend.presentation.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class GoogleLoginRequest {

   private String credential;

   @Pattern(regexp = "^(CARER|PROTECTED)$", message = "Rol inválido. Debe ser CARER o PROTECTED")
    private String role;
}