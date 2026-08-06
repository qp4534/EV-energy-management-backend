package com.ev_energy_management.backend.security;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private final JwtTokenProvider provider = new JwtTokenProvider("test-secret-value-for-jwt-signing", 3600000L);

    @Test
    void generateAndParseRoundTrip() {
        UUID userId = UUID.randomUUID();
        String token = provider.generateToken(userId, "관리자");

        Optional<AuthenticatedUser> parsed = provider.parseToken(token);

        assertTrue(parsed.isPresent());
        assertEquals(userId, parsed.get().userId());
        assertEquals("관리자", parsed.get().role());
    }

    @Test
    void expiredTokenIsRejected() throws InterruptedException {
        JwtTokenProvider shortLived = new JwtTokenProvider("test-secret-value-for-jwt-signing", -1000L);
        String token = shortLived.generateToken(UUID.randomUUID(), "관제자");

        assertTrue(provider.parseToken(token).isEmpty() || shortLived.parseToken(token).isEmpty());
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = provider.generateToken(UUID.randomUUID(), "관리자");
        String tampered = token.substring(0, token.length() - 1) + (token.endsWith("a") ? "b" : "a");

        assertTrue(provider.parseToken(tampered).isEmpty());
    }

    @Test
    void garbageTokenIsRejected() {
        assertTrue(provider.parseToken("not-a-jwt").isEmpty());
    }
}
