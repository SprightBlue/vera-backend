package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.model.DashboardData;
import com.unlam.verabackend.domain.model.Role;
import com.unlam.verabackend.domain.port.in.GetDashboardDataUseCase;
import com.unlam.verabackend.domain.port.out.AlertsRepository;
import com.unlam.verabackend.domain.port.out.AnalysisRepository;
import com.unlam.verabackend.domain.port.out.UserLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetDashboardDataUseCaseImpl implements GetDashboardDataUseCase {

    private final AnalysisRepository analysisRepository;
    private final AlertsRepository alertsRepository;
    private final UserLocationRepository userLocationRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardData execute(String email, Role role) {
        log.info("UseCase: Compilando Dashboard asimétrico para [{}] con rol de dominio [{}]", email, role);

        DashboardData.DashboardDataBuilder builder = DashboardData.builder();

        populateRoleSpecificMetrics(builder, email, role);

        return builder.build();
    }

    private void populateRoleSpecificMetrics(DashboardData.DashboardDataBuilder builder, String email, Role role) {
        switch (role) {
            case PROTECTED -> populateProtectedMetrics(builder, email);
            case CARER -> populateCarerMetrics(builder, email);
            default -> log.warn("UseCase Security Warning: El rol [{}] no posee vista de Dashboard asignada.", role);
        }
    }

    private void populateProtectedMetrics(DashboardData.DashboardDataBuilder builder, String email) {
        log.debug("UseCase: Extrayendo métricas exclusivas de PROTECTED (Análisis + Alertas Resueltas) para [{}]", email);

        builder.top3Analysis(analysisRepository.findTop3ByUserEmail(email))
                .analysisInLast24Hours(analysisRepository.countByUserEmailInLast24Hours(email))
                .top3ResolvedAlerts(alertsRepository.findTop3ResolvedAlertsByUserEmail(email))
                .resolvedAlertsInLast24Hours(alertsRepository.countResolvedAlertsInLast24Hours(email));
    }

    private void populateCarerMetrics(DashboardData.DashboardDataBuilder builder, String email) {
        log.debug("UseCase: Extrayendo métricas exclusivas de CARER (Alertas Activas + Monitoreo de Ubicaciones) para [{}]", email);

        builder.top3Alerts(alertsRepository.findTop3ActiveAlertsByCarerEmail(email))
                .alertsInLast24Hours(alertsRepository.countAlertsByCarerEmailInLast24Hours(email))
                .top3ConnectedUsers(userLocationRepository.findTop3LastConnectedByCarerEmail(email))
                .connectedUsersCount(userLocationRepository.countConnectedUsersByCarerEmail(email));
    }
}