package com.ev_energy_management.backend.dto.chat;

/** Internal request enriched with the authenticated Spring user identifier. */
public record FastApiChatRequest(
        String userId,
        String vehicleId,
        String message,
        String conversationId
) {}
