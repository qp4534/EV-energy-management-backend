package com.ev_energy_management.backend.dto.statsreport;

public record UserSummaryStatsDto(
        Long totalUsers,
        Long totalUsersDelta,
        Double activeRate,
        Double activeRateDelta,
        Long newUsersThisMonth,
        Long newUsersGeneral,
        Long newUsersController
) {}
