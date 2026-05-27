package com.unlam.verabackend.analysis.domain.ports.out;

import com.unlam.verabackend.analysis.domain.model.RiskAlert;

import java.util.UUID;

public interface RiskAlertRepositoryPort {
    RiskAlert save(RiskAlert alert);
    RiskAlert findById(UUID id);
}
