package com.ev_energy_management.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ExternalIntegrationDto(
        UUID integrationId,
        String name,
        String description,
        String apiKey,
        Boolean isStatus,
        OffsetDateTime lastConnectedAt,
        OffsetDateTime createdAt
) {}
