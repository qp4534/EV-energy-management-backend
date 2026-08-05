package com.ev_energy_management.backend.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BatteryOfferDto(
        UUID offerId,
        String buyerName,
        String businessType,
        BigDecimal offeredPrice,
        BigDecimal pricePerKwh,
        Integer rankOrder,
        String description,
        UUID batteryId
) {}
