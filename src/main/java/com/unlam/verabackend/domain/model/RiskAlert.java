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
    private Analysis analysis;
    private DomainUser caregiver;
    private boolean solved;
    private LocalDateTime createdAt;

    public static RiskAlert createActive(Analysis analysis, DomainUser caregiver) {
        return new RiskAlert(
                UUID.randomUUID(),
                analysis,
                caregiver,
                false,
                LocalDateTime.now()
        );
    }

    public void markAsSolved() {
        this.solved = true;
    }
}
