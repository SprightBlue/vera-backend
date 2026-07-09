package com.unlam.verabackend.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.unlam.verabackend.domain.model.DashboardData;
import com.unlam.verabackend.domain.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardResponse {
    private List<AnalysisResponse> top3Analysis;
    private Long analysisInLast24Hours;
    private List<AlertsResponse> top3ResolvedAlerts;
    private Long resolvedAlertsInLast24Hours;

    private List<AlertsResponse> top3Alerts;
    private Long alertsInLast24Hours;
    private List<UserLocationResponse> top3ConnectedUsers;
    private Long connectedUsersCount;

    public static DashboardResponse fromDomain(DashboardData data, Role role) {
        if (data == null) return null;

        DashboardResponseBuilder builder = DashboardResponse.builder();

        if (role == Role.PROTECTED) {
            return builder.top3Analysis(data.getTop3Analysis().stream().map(AnalysisResponse::fromDomain).toList())
                    .analysisInLast24Hours(data.getAnalysisInLast24Hours())
                    .top3ResolvedAlerts(data.getTop3ResolvedAlerts().stream().map(d -> AlertsResponse.fromDomain(d, role)).toList())
                    .resolvedAlertsInLast24Hours(data.getResolvedAlertsInLast24Hours())
                    .build();

        } else if (role == Role.CARER) {
            return builder.top3Alerts(data.getTop3Alerts().stream().map(d -> AlertsResponse.fromDomain(d, role)).toList())
                    .alertsInLast24Hours(data.getAlertsInLast24Hours())
                    .top3ConnectedUsers(data.getTop3ConnectedUsers().stream().map(UserLocationResponse::fromDomain).toList())
                    .connectedUsersCount(data.getConnectedUsersCount())
                    .build();
        }

        return builder.build();
    }
}