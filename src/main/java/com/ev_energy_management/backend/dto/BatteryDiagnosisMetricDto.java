package com.ev_energy_management.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BatteryDiagnosisMetricDto(
        UUID metricId,
        Integer remainingLifeScore,
        Integer dischargePowerScore,
        Integer chargeHealthScore,
        Integer voltageStabilityScore,
        OffsetDateTime diagnosedAt,
        UUID batteryId
) {}
