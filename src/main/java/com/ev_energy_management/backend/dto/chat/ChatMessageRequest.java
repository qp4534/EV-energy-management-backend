package com.ev_energy_management.backend.dto.chat;

import java.util.UUID;

/** Request exposed to authenticated web/app clients. */
public record ChatMessageRequest(
        UUID vehicleId,
        String message,
        String conversationId
) {}
