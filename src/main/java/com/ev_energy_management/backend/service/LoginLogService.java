package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.LoginLogDto;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class LoginLogService {

    private List<LoginLogDto> mockData() {
        return List.of(
                new LoginLogDto(UUID.randomUUID(), "192.168.0.11", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
                        "Seoul, KR", "SUCCESS", OffsetDateTime.now(), UUID.randomUUID(), null),
                new LoginLogDto(UUID.randomUUID(), "10.0.0.5", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15)",
                        "Busan, KR", "FAILED", OffsetDateTime.now(), UUID.randomUUID(), "비밀번호 불일치")
        );
    }

    public List<LoginLogDto> findAll() {
        return mockData();
    }

    public LoginLogDto findById(UUID logId) {
        return new LoginLogDto(logId, "192.168.0.11", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
                "Seoul, KR", "SUCCESS", OffsetDateTime.now(), UUID.randomUUID(), null);
    }

    public LoginLogDto create(LoginLogDto request) {
        return new LoginLogDto(UUID.randomUUID(), request.ipAddress(), request.userAgent(), request.location(),
                request.status(), OffsetDateTime.now(), request.userId(), request.failReason());
    }

    public LoginLogDto update(UUID logId, LoginLogDto request) {
        return new LoginLogDto(logId, request.ipAddress(), request.userAgent(), request.location(),
                request.status(), request.createdAt(), request.userId(), request.failReason());
    }

    public void delete(UUID logId) {
        // TODO: ERD 확정 후 실제 삭제 로직 연결
    }
}
