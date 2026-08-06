package com.ev_energy_management.backend.dto.auth;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record MeResponse(
        UUID userId,
        String email,
        String role,
        String name,
        String phone,
        String profileImageUrl,
        LocalDate birth,
        String permissions,
        Boolean emailVerified,
        OffsetDateTime createdAt
) {}
