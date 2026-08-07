package com.ev_energy_management.backend.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private TokenBlacklistService service;

    @Test
    void blacklistSwallowsRedisFailureInsteadOfThrowing() {
        service = new TokenBlacklistService(redisTemplate);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doThrow(new RedisConnectionFailureException("down"))
                .when(valueOperations).set(anyString(), anyString(), any(Duration.class));

        assertDoesNotThrow(() -> service.blacklist("token", Duration.ofSeconds(60)));
    }

    @Test
    void isBlacklistedTreatsRedisFailureAsNotBlacklisted() {
        service = new TokenBlacklistService(redisTemplate);
        when(redisTemplate.hasKey(anyString())).thenThrow(new RedisConnectionFailureException("down"));

        assertFalse(service.isBlacklisted("token"));
    }
}
