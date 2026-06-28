package com.unlam.verabackend.domain.model;

import com.unlam.verabackend.infrastructure.entity.TrustContact;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLocation {
    private UUID id;
    private TrustContact trustContact;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String locationText;
    private boolean isConnected;
    private LocalDateTime updatedAt;
}