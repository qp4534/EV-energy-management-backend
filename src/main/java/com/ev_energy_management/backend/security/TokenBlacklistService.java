package com.ev_energy_management.backend.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

// 로그아웃된 JWT를 Redis에 저장해 만료 전에도 강제로 무효화한다.
// 원문 토큰 대신 해시를 키로 써서 Redis에 자격증명이 그대로 남지 않게 한다.
@Service
public class TokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);
    private static final String KEY_PREFIX = "auth:blacklist:";

    private final StringRedisTemplate redisTemplate;

    public TokenBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Redis 장애가 로그인/로그아웃 같은 핵심 인증 흐름을 막으면 안 되므로, 연결 실패 시
    // 블랙리스트 기능만 조용히 건너뛴다(fail-open). 즉 Redis가 죽으면 "강제 무효화"만
    // 못 하고, 토큰은 원래 만료시간까지는 계속 정상 동작한다.
    public void blacklist(String token, Duration ttl) {
        if (ttl.isNegative() || ttl.isZero()) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(KEY_PREFIX + hash(token), "1", ttl);
        } catch (DataAccessException e) {
            log.warn("Redis unavailable, skipping token blacklist registration", e);
        }
    }

    public boolean isBlacklisted(String token) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + hash(token)));
        } catch (DataAccessException e) {
            log.warn("Redis unavailable, treating token as not blacklisted", e);
            return false;
        }
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
