package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.application.service.NotificationService;
import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.Alerts;
import com.unlam.verabackend.domain.model.NotificationsType;
import com.unlam.verabackend.domain.model.RiskLevel;
import com.unlam.verabackend.domain.port.in.ManageAlertsUseCase;
import com.unlam.verabackend.domain.port.out.AlertsRepository;
import com.unlam.verabackend.domain.port.out.RtcProvider;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageAlertsUseCaseImpl implements ManageAlertsUseCase {

    private final AlertsRepository alertsRepository;
    private final TrustContactRepository trustContactRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final RtcProvider rtcProvider;

    @Override
    @Transactional(readOnly = true)
    public Page<Alerts> getAlertsHistory(String carerEmail, Boolean isResolved, RiskLevel riskLevel, String search, int page) {
        log.info("UseCase: Compilando historial de alertas asociadas al cuidador [{}]", carerEmail);

        List<Long> contactIds = getTrustContactIdsByEmail(carerEmail);
        if (contactIds.isEmpty()) {
            log.warn("UseCase: El cuidador [{}] no registra relaciones de confianza asignadas. Retornando página vacía.", carerEmail);
            return Page.empty(PageRequest.of(page, 10));
        }

        log.debug("UseCase: Ejecutando consulta por criterios para {} IDs de contacto vinculados.", contactIds.size());
        return alertsRepository.findByCriteria(contactIds, isResolved, riskLevel, search, page);
    }

    @Override
    @Transactional(readOnly = true)
    public Alerts getAlertDetail(UUID id, String carerEmail) {
        log.info("UseCase: Solicitando desglose confidencial de la alerta ID [{}]", id);
        return validateAndGetOwnedAlert(id, carerEmail);
    }

    @Override
    @Transactional
    public void deleteAlert(UUID id, String carerEmail) {
        log.info("UseCase: Solicitando remoción definitiva de la alerta ID [{}] por [{}]", id, carerEmail);

        Alerts alert = validateAndGetOwnedAlert(id, carerEmail);

        alertsRepository.deleteById(alert.getId());
        log.info("UseCase: Alerta ID [{}] removida correctamente de la persistencia.", id);

        if (alert.getTrustContact() != null) {
            String emailDelCarer = alert.getTrustContact().getCarer().getEmail();
            log.info("UseCase: Sincronizando eliminación en el Dashboard del Carer [{}]", emailDelCarer);
            rtcProvider.publishCarerDashboardAlertDeleted(emailDelCarer, alert.getId());

            if (alert.getTrustContact().getProtectedUser() != null) {
                String emailDelProtected = alert.getTrustContact().getProtectedUser().getEmail();
                log.info("UseCase: Sincronizando eliminación en el Dashboard del Protected [{}]", emailDelProtected);
                rtcProvider.publishProtectedDashboardAlertDeleted(emailDelProtected, alert.getId());
            }
        }
    }

    @Override
    @Transactional
    public void resolveAlert(UUID id, String carerEmail) {
        log.info("UseCase: Ejecutando transición de estado a RESUELTA para la alerta ID [{}] por [{}]", id, carerEmail);
        Alerts alert = validateAndGetOwnedAlert(id, carerEmail);
        alert.setResolved(true);
        alertsRepository.save(alert, alert.getTrustContact().getId());
        dispatchResolutionNotification(alert);

        if (alert.getTrustContact() != null && alert.getTrustContact().getProtectedUser() != null) {
            String emailDelProtected = alert.getTrustContact().getProtectedUser().getEmail();
            log.info("UseCase: Sincronizando resolución únicamente en el Dashboard del Protected [{}]", emailDelProtected);
            rtcProvider.publishProtectedDashboardResolvedAlertUpdate(emailDelProtected, alert);
        }
    }

    private List<Long> getTrustContactIdsByEmail(String carerEmail) {
        log.debug("UseCase: Extrayendo identificadores de vínculos para el email [{}]", carerEmail);
        var user = userRepository.findByEmail(carerEmail)
                .orElseThrow(() -> {
                    log.error("UseCase Error: No se encontró registro de usuario para el email [{}]", carerEmail);
                    return new ResourceNotFoundException("Usuario no encontrado con email: " + carerEmail);
                });

        return trustContactRepository.findByCarerId(user.getId()).stream()
                .map(TrustContact::getId)
                .toList();
    }

    private Alerts validateAndGetOwnedAlert(UUID id, String carerEmail) {
        Alerts alert = alertsRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("UseCase Error: La alerta ID [{}] solicitada no existe.", id);
                    return new ResourceNotFoundException("La alerta solicitada no existe.");
                });

        if (!alert.getTrustContact().getCarer().getEmail().equalsIgnoreCase(carerEmail)) {
            log.warn("ALERTA DE SEGURIDAD: El operador [{}] intentó acceder de forma ilegítima a la alerta privada ID [{}]",
                    carerEmail, id);
            throw new AccessDeniedException("No tenés permisos para interactuar con esta alerta.");
        }
        return alert;
    }

    private void dispatchResolutionNotification(Alerts alert) {
        log.debug("UseCase: Construyendo payload de notificación de cierre para el protegido [{}]", alert.getTrustContact().getProtectedUser().getEmail());
        Map<String, Object> payload = Map.of("alertId", alert.getId().toString());

        notificationService.createAndDispatch(
                alert.getTrustContact().getProtectedUser(),
                NotificationsType.ALERT_SOLVED,
                alert.getTrustContact().getCarer().getFullName(),
                payload
        );
        log.info("UseCase: Notificación distribuida con éxito al bus de tiempo real.");
    }
}