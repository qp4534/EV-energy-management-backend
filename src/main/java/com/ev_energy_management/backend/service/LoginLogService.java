package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.LoginLogDto;
import com.ev_energy_management.backend.entity.LoginLogEntity;
import com.ev_energy_management.backend.repository.LoginLogRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LoginLogService {

    private final LoginLogRepository loginLogRepository;

    public LoginLogService(LoginLogRepository loginLogRepository) {
        this.loginLogRepository = loginLogRepository;
    }

    public List<LoginLogDto> findAll() {
        return loginLogRepository.findAll().stream().map(this::toDto).toList();
    }

    public LoginLogDto findById(UUID logId) {
        return toDto(loginLogRepository.findById(logId)
                .orElseThrow(() -> new EntityNotFoundException("Login log not found: " + logId)));
    }

    public LoginLogDto create(LoginLogDto request) {
        LoginLogEntity entity = LoginLogEntity.builder()
                .ipAddress(request.ipAddress())
                .userAgent(request.userAgent())
                .location(request.location())
                .status(request.status() != null ? request.status() : "SUCCESS")
                .userId(request.userId())
                .failReason(request.failReason())
                .build();
        return toDto(loginLogRepository.save(entity));
    }

    public LoginLogDto update(UUID logId, LoginLogDto request) {
        LoginLogEntity entity = loginLogRepository.findById(logId)
                .orElseThrow(() -> new EntityNotFoundException("Login log not found: " + logId));
        entity.setIpAddress(request.ipAddress());
        entity.setUserAgent(request.userAgent());
        entity.setLocation(request.location());
        entity.setStatus(request.status());
        entity.setUserId(request.userId());
        entity.setFailReason(request.failReason());
        return toDto(loginLogRepository.save(entity));
    }

    public void delete(UUID logId) {
        loginLogRepository.deleteById(logId);
    }

    private LoginLogDto toDto(LoginLogEntity entity) {
        return new LoginLogDto(
                entity.getLogId(),
                entity.getIpAddress(),
                entity.getUserAgent(),
                entity.getLocation(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUserId(),
                entity.getFailReason()
        );
    }
}
