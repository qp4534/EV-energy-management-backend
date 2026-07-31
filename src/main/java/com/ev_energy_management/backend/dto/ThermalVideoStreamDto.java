package com.ev_energy_management.backend.dto;

import java.util.UUID;

public record ThermalVideoStreamDto(
        UUID thermalId,
        String videoUrl,
        String metadata
) {}
