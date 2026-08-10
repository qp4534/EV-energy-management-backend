package com.ev_energy_management.backend.dto.statsreport;

public record FireSummaryStatsDto(
        Long alertCount,
        Long alertCountDelta,
        String topAbnormalType,
        Long topAbnormalTypeCount
) {}
