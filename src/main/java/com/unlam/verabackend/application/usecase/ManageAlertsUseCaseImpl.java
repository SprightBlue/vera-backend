package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.application.service.SseService;
import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.Alerts;
import com.unlam.verabackend.domain.model.NotificationsType;
import com.unlam.verabackend.domain.port.in.ManageAlertsUseCase;
import com.unlam.verabackend.domain.port.out.AlertsRepository;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManageAlertsUseCaseImpl implements ManageAlertsUseCase {

    private final AlertsRepository alertsRepository;
    private final TrustContactRepository trustContactRepository;
    private final UserRepository userRepository;
    private final SseService sseService;

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

        return alertsRepository.findByTrustContactIdsCreatedAtDesc(contactIds, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Alerts> getHistoryByCarerEmailAndIsResolved(String email, boolean isResolved, Pageable pageable) {
        List<Long> contactIds = getTrustContactIdsByEmail(email);
        if (contactIds.isEmpty()) return Page.empty(pageable);

        return alertsRepository.findByTrustContactIdsAndIsResolvedCreatedAtDesc(contactIds, isResolved, pageable);
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

    @Override
    @Transactional
    public void resolveAlert(UUID id, String carerEmail) {
        Alerts alert = getAlertDetail(id, carerEmail);
        alert.resolve();
        alertsRepository.save(alert, alert.getTrustContact().getId());

        Map<String, Object> payload = Map.of("alertId", alert.getId().toString());

        sseService.createAndSendNotification(
                alert.getTrustContact().getProtectedUser(),
                NotificationsType.ALERT_SOLVED,
                alert.getTrustContact().getCarer().getFullName(),
                payload
        );
    }
}