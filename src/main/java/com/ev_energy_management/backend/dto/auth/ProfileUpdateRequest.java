package com.ev_energy_management.backend.dto.auth;

public record ProfileUpdateRequest(
        String name,
        String phone,
        String profileImageUrl,
        Boolean pushEnabled,
        String currentPassword,
        String newPassword
) {}
