package com.unlam.verabackend.domain.ports.in;

import com.unlam.verabackend.domain.model.RiskAlert;

import java.util.UUID;

public interface MarkAlertAsReceivedUseCase {
    RiskAlert markAsReceived(UUID alertId);
}
