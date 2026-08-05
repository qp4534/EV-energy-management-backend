package com.ev_energy_management.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BatteryPassportDto(
        UUID batteryId,
        String manufacturer,
        String batteryType,
        String ratedCapacity,
        BigDecimal sohScore,
        Integer chargeCycles,
        BigDecimal currentTemp,
        LocalDate lastInspectedAt,
        UUID carId,
        String batteryLevel,
        Integer remainingCycles,
        Integer totalCycles,
        String reuseStatus,
        String gradeDetail,
        BigDecimal reliabilityScore,
        String reuseProbabilities,
        BigDecimal voltage,
        BigDecimal current,
        BigDecimal rul,
        LocalDate manufacturedAt,
        LocalDate installedAt
) {}
