package com.ev_energy_management.backend.dto;

import java.util.UUID;

public record CarDto(
        UUID carId,
        String carNumber,
        String model,
        String vin,
        UUID userId
) {}
