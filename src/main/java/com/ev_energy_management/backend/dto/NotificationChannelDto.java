package com.ev_energy_management.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationChannelDto(
        UUID channelId,
        String channelName,
        Boolean isActive,
        OffsetDateTime updatedAt
) {}
