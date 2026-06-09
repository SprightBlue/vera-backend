package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.Alerts;
import com.unlam.verabackend.domain.port.in.ManageAlertsUseCase;
import com.unlam.verabackend.domain.port.out.AlertsRepository;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ManageAlertsUseCaseImpl implements ManageAlertsUseCase {

    private final AlertsRepository alertsRepository;
    private final TrustContactRepository trustContactRepository;
    private final UserRepository userRepository;

    public ManageAlertsUseCaseImpl(AlertsRepository alertsRepository,
                                   TrustContactRepository trustContactRepository,
                                   UserRepository userRepository) {
        this.alertsRepository = alertsRepository;
        this.trustContactRepository = trustContactRepository;
        this.userRepository = userRepository;
    }

    private List<Long> getTrustContactIdsByEmail(String carerEmail) {
        var user = userRepository.findByEmail(carerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + carerEmail));

        List<TrustContact> contacts = trustContactRepository.findByCarerId(user.getId());
        return contacts.stream().map(TrustContact::getId).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Alerts> getHistoryByCarerEmail(String email, Pageable pageable) {
        List<Long> contactIds = getTrustContactIdsByEmail(email);
        if (contactIds.isEmpty()) return Page.empty(pageable);

        return alertsRepository.findByTrustContactIds(contactIds, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Alerts> getHistoryByCarerEmailAndIsResolved(String email, boolean isResolved, Pageable pageable) {
        List<Long> contactIds = getTrustContactIdsByEmail(email);
        if (contactIds.isEmpty()) return Page.empty(pageable);

        return alertsRepository.findByTrustContactIdsAndIsResolved(contactIds, isResolved, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Alerts getAlertDetail(UUID id, String carerEmail) {
        Alerts alert = alertsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("La alerta solicitada no existe."));

        if (!alert.getTrustContact().getCarer().getEmail().equals(carerEmail)) {
            throw new AccessDeniedException("No tenés permisos para ver esta alerta.");
        }
        return alert;
    }

    @Override
    @Transactional
    public void deleteAlert(UUID id, String carerEmail) {
        Alerts alert = getAlertDetail(id, carerEmail);
        alertsRepository.deleteById(alert.getId());
    }
}