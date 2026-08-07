package com.ev_energy_management.backend.dto.auth;

public record PasswordResetRequest(String email, String newPassword) {}
