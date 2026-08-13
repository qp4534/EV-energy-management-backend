package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.TwinAccessTokenResponse;
import com.ev_energy_management.backend.security.AuthenticatedUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class TwinAccessTokenService {

    private static final String ISSUER = "ev-energy-backend";
    private static final String AUDIENCE = "ev-ai-twin";
    private static final String SCOPE = "twin:read";

    private final SecretKey key;
    private final long expirationSeconds;

    public TwinAccessTokenService(
            @Value("${twin.access-secret}") String secret,
            @Value("${twin.access-expiration-seconds:300}") long expirationSeconds
    ) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException(
                    "TWIN_TICKET_SECRET must contain at least 32 characters");
        }
        if (expirationSeconds < 30 || expirationSeconds > 600) {
            throw new IllegalArgumentException(
                    "Twin access expiration must be between 30 and 600 seconds");
        }
        this.key = Keys.hmacShaKeyFor(sha256(secret));
        this.expirationSeconds = expirationSeconds;
    }

    public TwinAccessTokenResponse issue(AuthenticatedUser user, UUID carId) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(expirationSeconds);
        String token = Jwts.builder()
                .subject(user.userId().toString())
                .issuer(ISSUER)
                .audience().add(AUDIENCE).and()
                .claim("vehicle_id", carId.toString())
                .claim("scope", SCOPE)
                .claim("role", user.role())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
        return new TwinAccessTokenResponse(token, expiresAt);
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }
}
