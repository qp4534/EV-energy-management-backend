package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.TwinAccessTokenResponse;
import com.ev_energy_management.backend.security.AuthenticatedUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TwinAccessTokenServiceTest {

    private static final String SECRET =
            "test-twin-ticket-secret-with-more-than-thirty-two-characters";

    @Test
    void issuesShortLivedVehicleScopedReadTicket() throws Exception {
        TwinAccessTokenService service = new TwinAccessTokenService(SECRET, 300);
        UUID userId = UUID.fromString("00000000-0000-4000-8000-000000000001");
        UUID carId = UUID.fromString("11111111-1111-4111-8111-111111111111");

        TwinAccessTokenResponse response = service.issue(
                new AuthenticatedUser(userId, "관제자"), carId);

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(
                        MessageDigest.getInstance("SHA-256")
                                .digest(SECRET.getBytes(StandardCharsets.UTF_8))))
                .build()
                .parseSignedClaims(response.accessToken())
                .getPayload();
        assertEquals(userId.toString(), claims.getSubject());
        assertEquals(carId.toString(), claims.get("vehicle_id", String.class));
        assertEquals("twin:read", claims.get("scope", String.class));
        assertEquals("ev-energy-backend", claims.getIssuer());
        assertTrue(claims.getAudience().contains("ev-ai-twin"));
        assertTrue(response.expiresAt().isAfter(Instant.now()));
    }

    @Test
    void rejectsMissingOrWeakTicketSecret() {
        assertThrows(IllegalArgumentException.class,
                () -> new TwinAccessTokenService("", 300));
        assertThrows(IllegalArgumentException.class,
                () -> new TwinAccessTokenService("too-short", 300));
    }
}
