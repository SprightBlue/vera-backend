package com.unlam.verabackend.analysis.domain.ports.in;

import com.unlam.verabackend.analysis.domain.model.RiskAlert;

import java.util.UUID;

public interface MarkAlertAsReceivedUseCase {
    RiskAlert markAsReceived(UUID alertId);
}
