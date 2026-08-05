package com.ev_energy_management.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AnomalyLogDto(
        UUID anomalyId,
        String abnormalType,
        String sourceType,
        String triggerValue,
        OffsetDateTime detectedAt,
        String riskLevel,
        UUID carId,
        UUID sessionId
) {}
