package com.ev_energy_management.backend.dto.auth;

public record VerifyEmailCodeRequest(String email, String code) {}
