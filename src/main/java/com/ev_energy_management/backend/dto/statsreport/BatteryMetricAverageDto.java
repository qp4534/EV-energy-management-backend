package com.ev_energy_management.backend.dto.statsreport;

public record BatteryMetricAverageDto(
        Double remainingLifeAvg,
        Double dischargePowerAvg,
        Double chargeHealthAvg,
        Double voltageStabilityAvg
) {}
