package com.unlam.verabackend.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterNotificationTokenRequest {
    @NotBlank
    private String token;

    @NotBlank
    private String platform;
}
