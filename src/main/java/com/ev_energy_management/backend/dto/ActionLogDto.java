package com.ev_energy_management.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ActionLogDto(
        UUID actionId,
        String actionType,
        String targetType,
        UUID targetId,
        String detail,
        OffsetDateTime createdAt,
        UUID userId
) {}
