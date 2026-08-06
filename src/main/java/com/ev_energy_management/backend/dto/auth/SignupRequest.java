package com.ev_energy_management.backend.dto.auth;

import java.time.LocalDate;
import java.util.List;

public record SignupRequest(
        String email,
        String password,
        String name,
        String phone,
        LocalDate birth,
        String role,
        List<String> consentedTerms
) {}
