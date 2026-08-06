package com.ev_energy_management.backend.security;

import java.util.UUID;

public record AuthenticatedUser(UUID userId, String role) {
}
