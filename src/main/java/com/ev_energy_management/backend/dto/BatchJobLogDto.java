package com.ev_energy_management.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BatchJobLogDto(
        UUID batchLogId,
        String runType,
        String status,
        String message,
        OffsetDateTime executedAt,
        String jobId
) {}
