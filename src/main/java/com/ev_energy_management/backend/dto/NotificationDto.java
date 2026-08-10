package com.ev_energy_management.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationDto(
        UUID notificationId,
        String riskLevel,
        String title,
        String body,
        Boolean isRead,
        OffsetDateTime createdAt,
        UUID userId,
        UUID carId,
        UUID reportId
) {}
