package com.ev_energy_management.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CarDto(
        UUID carId,
        String carNumber,
        String model,
        String vin,
        String nickname,
        String imageUrl,
        Boolean isPrimary,
        OffsetDateTime createdAt,
        UUID userId
) {}
