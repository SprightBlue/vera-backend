package com.unlam.verabackend.domain.model;

import com.unlam.verabackend.infrastructure.entity.TrustContact;
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
    private List<Alerts> top3Alerts;

    private long analysisCountSince;
    private long alertsCountSince;
    private long resolvedAlertsCountSince;

    private TrustContact latestTrustContact;
    private Chats latestUpdatedChat;
}