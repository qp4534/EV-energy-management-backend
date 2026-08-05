package com.ev_energy_management.backend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ChargerDto(
        UUID chargerId,
        String chargerType,
        BigDecimal ratedPowerKw,
        String status,
        Integer queueLength,
        Integer waitingTimeMin,
        OffsetDateTime updatedAt,
        UUID chargeId
) {}
