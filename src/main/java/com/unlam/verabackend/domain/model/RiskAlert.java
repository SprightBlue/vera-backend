package com.unlam.verabackend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RiskAlert {
    private UUID id;
    private UUID analysisId;
    private Long caregiverId;
    private boolean received;
    private LocalDateTime createdAt;

    public void markAsReceived() {
        if (this.received) return;
        this.received = true;
    }
}
