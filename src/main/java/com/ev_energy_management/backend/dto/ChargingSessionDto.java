package com.ev_energy_management.backend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ChargingSessionDto(
        UUID sessionId,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        String changeState,
        UUID carId,
        UUID chargerId,
        BigDecimal startSoc,
        BigDecimal endSoc
) {}
