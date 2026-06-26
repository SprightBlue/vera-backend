package com.unlam.verabackend.presentation.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LocationUpdateDto {
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String locationText;
}