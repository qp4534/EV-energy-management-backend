package com.ev_energy_management.backend.dto.chat;

/** Internal request enriched only from the authenticated Spring principal. */
public record FastApiChatRequest(
        String userId,
        String actorRole,
        String vehicleId,
        String message,
        String conversationId
) {}
