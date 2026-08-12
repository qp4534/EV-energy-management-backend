package com.ev_energy_management.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LoginLogDto(
        UUID logId,
        String ipAddress,
        String userAgent,
        String location,
        String status,
        OffsetDateTime createdAt,
        UUID userId,
        String userName,
        String userRole,
        String failReason
) {}
