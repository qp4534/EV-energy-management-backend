package com.ev_energy_management.backend.dto;

import java.time.OffsetDateTime;

public record BatchJobDto(
        String jobId,
        String jobName,
        String cycle,
        String status,
        OffsetDateTime lastRunAt,
        OffsetDateTime nextRunAt
) {}
