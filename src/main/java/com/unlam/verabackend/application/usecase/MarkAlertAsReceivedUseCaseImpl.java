package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.model.RiskAlert;
import com.unlam.verabackend.domain.ports.in.MarkAlertAsReceivedUseCase;
import com.unlam.verabackend.domain.ports.out.RiskAlertRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class MarkAlertAsReceivedUseCaseImpl implements MarkAlertAsReceivedUseCase {

    private final RiskAlertRepositoryPort riskAlertRepositoryPort;

    public MarkAlertAsReceivedUseCaseImpl(RiskAlertRepositoryPort riskAlertRepositoryPort) {
        this.riskAlertRepositoryPort = riskAlertRepositoryPort;
    }

    @Override
    @Transactional
    public RiskAlert markAsReceived(UUID alertId) {
        if (alertId == null) throw new IllegalArgumentException("El ID de la alerta no puede ser nulo");

        RiskAlert alert = riskAlertRepositoryPort.findById(alertId);

        if (alert == null) throw new IllegalStateException("No se encontro la alerta con el ID especificado: " + alertId);

        alert.markAsReceived();

        return riskAlertRepositoryPort.save(alert);
    }
}
