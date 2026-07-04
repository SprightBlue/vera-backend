package com.unlam.verabackend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioOption {
    private UUID id;
    private String label;
    private boolean correct;
    private String feedback;
    private String warningSignals;
    private int displayOrder;
}