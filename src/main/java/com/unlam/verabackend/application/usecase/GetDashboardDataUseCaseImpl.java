package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.model.DashboardData;
import com.unlam.verabackend.domain.model.Role;
import com.unlam.verabackend.domain.port.in.GetDashboardDataUseCase;
import com.unlam.verabackend.domain.port.out.AlertsRepository;
import com.unlam.verabackend.domain.port.out.AnalysisRepository;
import com.unlam.verabackend.domain.port.out.ChatsRepository;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetDashboardDataUseCaseImpl implements GetDashboardDataUseCase {

    private final AnalysisRepository analysisRepository;
    private final AlertsRepository alertsRepository;
    private final ChatsRepository chatsRepository;
    private final TrustContactRepository trustContactRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardData execute(String email, Role role) {
        log.info("UseCase: Compilando Dashboard asimétrico para [{}] con rol de dominio [{}]", email, role);

        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        DashboardData.DashboardDataBuilder builder = DashboardData.builder();

        log.debug("UseCase: Calculando contadores globales de la última semana para [{}]", email);
        builder.analysisCountSince(analysisRepository.countByUserEmailSince(email, oneWeekAgo))
                .alertsCountSince(alertsRepository.countAlertsByEmailSince(email, oneWeekAgo))
                .resolvedAlertsCountSince(alertsRepository.countResolvedAlertsByEmailSince(email, oneWeekAgo));

        log.debug("UseCase: Extrayendo último chat actualizado para [{}]", email);
        chatsRepository.findLastUpdatedByUserEmail(email)
                .ifPresent(builder::latestUpdatedChat);

        populateLatestContactByRole(builder, email, role);

        populateRoleSpecificLists(builder, email, role);

        return builder.build();
    }

    private void populateLatestContactByRole(DashboardData.DashboardDataBuilder builder, String email, Role role) {
        log.debug("UseCase: Buscando último TrustContact agregado según rol [{}] para [{}]", role, email);
        if (role == Role.CARER) {
            trustContactRepository.findFirstByCarer_EmailOrderByCreatedAtDesc(email)
                    .ifPresent(builder::latestTrustContact);
        } else if (role == Role.PROTECTED) {
            trustContactRepository.findFirstByProtectedUser_EmailOrderByCreatedAtDesc(email)
                    .ifPresent(builder::latestTrustContact);
        }
    }

    private void populateRoleSpecificLists(DashboardData.DashboardDataBuilder builder, String email, Role role) {
        switch (role) {
            case PROTECTED -> {
                log.debug("UseCase: Extrayendo listado exclusivo de PROTECTED (Análisis) para [{}]", email);
                builder.top3Analysis(analysisRepository.findTop3ByUserEmail(email));
            }
            case CARER -> {
                log.debug("UseCase: Extrayendo listado exclusivo de CARER (Alertas Activas) para [{}]", email);
                builder.top3Alerts(alertsRepository.findTop3ActiveAlertsByCarerEmail(email));
            }
            default -> log.warn("UseCase Security Warning: El rol [{}] no posee vista de Dashboard asignada.", role);
        }
    }
}