package com.ev_energy_management.backend.dto;

import java.util.UUID;

public record NotificationMatrixDto(
        UUID matrixId,
        String riskLevel,
        UUID isEnabled,
        UUID channelId
) {}
