package com.unlam.verabackend.presentation.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LocationRequest {
    private BigDecimal latitude;
    private BigDecimal longitude;
}