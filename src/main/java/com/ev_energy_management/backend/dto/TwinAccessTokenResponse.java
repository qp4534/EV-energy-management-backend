package com.ev_energy_management.backend.dto;

import java.time.Instant;

public record TwinAccessTokenResponse(
        String accessToken,
        Instant expiresAt
) {
}
