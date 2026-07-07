package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.application.service.SseService;
import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.Alerts;
import com.unlam.verabackend.domain.model.NotificationsType;
import com.unlam.verabackend.domain.model.RiskLevel;
import com.unlam.verabackend.domain.port.in.ManageAlertsUseCase;
import com.unlam.verabackend.domain.port.out.AlertsRepository;
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
import com.unlam.verabackend.domain.model.Role;
import com.unlam.verabackend.infrastructure.entity.User;

import java.time.LocalDateTime;
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
    private final SseService sseService;

    private List<Long> getTrustContactIdsByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<TrustContact> contacts;

        if (user.getRole() == Role.CARER) {
            contacts = trustContactRepository.findByCarerId(user.getId());
        } else {
            contacts = trustContactRepository.findByProtectedUserId(user.getId());
        }

        return contacts.stream()
                .map(TrustContact::getId)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Alerts> getAlertsHistory(String carerEmail, Boolean isResolved, RiskLevel riskLevel, String search, int page) {
        List<Long> contactIds = getTrustContactIdsByEmail(carerEmail);
        if (contactIds.isEmpty()) {
            log.warn("El cuidador {} no posee ningún contacto de confianza asignado.", carerEmail);
            return Page.empty(PageRequest.of(page, 10));
        }
        return alertsRepository.findByCriteria(contactIds, isResolved, riskLevel, search, page);
    }

    @Override
    @Transactional(readOnly = true)
    public Alerts getAlertDetail(UUID id, String carerEmail) {
        Alerts alert = alertsRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Alerta no encontrada con ID: {}", id);
                    return new ResourceNotFoundException("La alerta solicitada no existe.");
                });

        String carer = alert.getTrustContact().getCarer().getEmail();
        String protectedUser = alert.getTrustContact().getProtectedUser().getEmail();

        if (!carerEmail.equals(carer) && !carerEmail.equals(protectedUser)) {
            log.error("VIOLACIÓN DE SEGURIDAD: El usuario {} intentó acceder a la alerta {} sin permisos.", carerEmail, id);
            throw new AccessDeniedException("No tenés permisos para ver esta alerta.");
        }

        return alert;
    }

    @Override
    @Transactional
    public void deleteAlert(UUID id, String carerEmail) {
        Alerts alert = getAlertDetail(id, carerEmail);

        if (!alert.getTrustContact().getCarer().getEmail().equals(carerEmail)) {
            log.warn("ACCESO DENEGADO: El usuario {} intentó eliminar la alerta {} pero no es el cuidador asignado.", carerEmail, id);
            throw new AccessDeniedException("Solo el cuidador puede eliminar una alerta.");
        }

        alertsRepository.deleteById(alert.getId());
        log.info("Alerta ID: {} eliminada correctamente por cuidador: {}", id, carerEmail);
    }

    @Override
    @Transactional
    public void resolveAlert(UUID id, String carerEmail) {
        Alerts alert = getAlertDetail(id, carerEmail);

        if (!alert.getTrustContact().getCarer().getEmail().equals(carerEmail)) {
            log.warn("ACCESO DENEGADO: El usuario {} intentó resolver la alerta {} pero no es el cuidador asignado.", carerEmail, id);
            throw new AccessDeniedException("Solo el cuidador puede resolver una alerta.");
        }

        alertsRepository.resolveAlert(alert.getId(), LocalDateTime.now());
        log.info("Alerta ID: {} marcada como RESUELTA por el cuidador: {}", id, carerEmail);

        Map<String, Object> payload = Map.of("alertId", alert.getId().toString());
        sseService.createAndSendNotification(
                alert.getTrustContact().getProtectedUser(),
                NotificationsType.ALERT_SOLVED,
                alert.getTrustContact().getCarer().getFullName(),
                payload);
        log.info("Notificación SSE enviada al usuario protegido con motivo de resolución de alerta.");
    }
}