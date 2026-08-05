package com.ev_energy_management.backend.dto;

import java.time.OffsetDateTime;

public record NotificationChannelDto(
        String channelId,
        String channelName,
        Boolean isActive,
        OffsetDateTime updatedAt
) {}
