package com.ev_energy_management.backend.dto.auth;

import java.time.LocalDate;

public record FindEmailRequest(String name, String phone, LocalDate birth, String role) {}
