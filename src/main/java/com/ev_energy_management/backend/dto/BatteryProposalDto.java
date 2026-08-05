package com.ev_energy_management.backend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record BatteryProposalDto(
        UUID proposalId,
        BigDecimal totalPrice,
        BigDecimal pricePerKwh,
        String capacityRange,
        String suitabilityReason,
        String noticeText,
        OffsetDateTime createdAt,
        UUID batteryId
) {}
