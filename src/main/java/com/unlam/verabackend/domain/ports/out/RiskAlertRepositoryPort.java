package com.unlam.verabackend.domain.ports.out;

import com.unlam.verabackend.domain.model.RiskAlert;

import java.util.UUID;

public interface RiskAlertRepositoryPort {
    RiskAlert save(RiskAlert alert);
    RiskAlert findById(UUID id);
}
