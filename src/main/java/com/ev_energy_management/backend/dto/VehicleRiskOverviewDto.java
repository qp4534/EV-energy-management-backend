package com.ev_energy_management.backend.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record VehicleRiskOverviewDto(
        VehicleRiskSummaryDto summary,
        List<VehicleRiskDto> vehicles,
        List<DailyRiskCountDto> dailyRiskCounts
) {
    public record VehicleRiskSummaryDto(
            long total,
            long emergency,
            long warning,
            long caution,
            long normal
    ) {}

    public record VehicleRiskDto(
            UUID carId,
            String carNumber,
            String model,
            String vin,
            String riskLevel,
            String abnormalType,
            OffsetDateTime detectedAt
    ) {}

    public record DailyRiskCountDto(
            String date,
            long count
    ) {}
}
