package com.ev_energy_management.backend.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ChargingStationDto(
        UUID chargeId,
        String region,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        String name,
        Integer slowChargerCount,
        Integer fastChargerCount,
        Integer availableCount,
        Integer minQueueLength,
        Integer minWaitingTime
) {}
