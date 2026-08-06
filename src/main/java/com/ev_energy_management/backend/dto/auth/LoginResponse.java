package com.ev_energy_management.backend.dto.auth;

import java.util.UUID;

public record LoginResponse(
        String token,
        String role,
        UUID userId,
        String name
) {}
