package com.ev_energy_management.backend.dto.statsreport;

import java.time.LocalDate;

public record RecentDiagnosisDto(
        String batteryId,
        String grade,
        Double sohScore,
        LocalDate inspectedAt
) {}
