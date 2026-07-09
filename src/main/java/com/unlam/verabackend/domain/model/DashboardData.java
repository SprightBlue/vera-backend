package com.unlam.verabackend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardData {
    private List<Analysis> top3Analysis;
    private long analysisInLast24Hours;
    private List<Alerts> top3ResolvedAlerts;
    private long resolvedAlertsInLast24Hours;

    private List<Alerts> top3Alerts;
    private long alertsInLast24Hours;
    private List<UserLocation> top3ConnectedUsers;
    private long connectedUsersCount;
}