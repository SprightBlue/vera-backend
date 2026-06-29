package com.unlam.verabackend.presentation.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileResponse {

    private Long id;

    private String fullName;

    private String email;

    private String phone;

    private String country;

    private String role;

    private String imageUrl;

}