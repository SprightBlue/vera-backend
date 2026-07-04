package com.unlam.verabackend.presentation.dto;

import java.util.List;

public record TrainingProgressResponse(
        TrainingStatsResponse stats,
        List<DailyProgressPoint> dailyProgress,
        List<RecentSessionSummary> recentSessions
) {
    public record DailyProgressPoint(String date, int correctRate, int total) {}
    public record RecentSessionSummary(String sessionId, String scenarioTitle, String scenarioType, boolean correct, String completedAt) {}
}