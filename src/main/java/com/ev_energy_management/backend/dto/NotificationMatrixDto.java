package com.ev_energy_management.backend.dto;

import java.util.UUID;

public record NotificationMatrixDto(
        UUID matrixId,
        String riskLevel,
        Boolean isEnabled,
        UUID channelId
) {}
