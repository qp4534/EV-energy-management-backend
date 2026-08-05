package com.ev_energy_management.backend.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UserDto(
        UUID userId,
        String email,
        String passwordHash,
        String role,
        LocalDate birth,
        Boolean isAgree,
        Integer loginFailed,
        Boolean isLocked,
        String permissions,
        String name,
        String phone,
        String profileImageUrl,
        OffsetDateTime createdAt,
        Boolean emailVerified,
        OffsetDateTime lockedAt
) {}
