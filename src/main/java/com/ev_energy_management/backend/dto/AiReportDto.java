package com.ev_energy_management.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AiReportDto(
        UUID reportId,
        String title,
        String reportData,
        String reportType,
        OffsetDateTime createdAt,
        UUID carId,
        UUID anomalyId,
        Boolean isRead
) {}
