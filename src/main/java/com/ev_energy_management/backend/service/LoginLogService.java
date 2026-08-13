package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.LoginLogDto;
import com.ev_energy_management.backend.entity.LoginLogEntity;
import com.ev_energy_management.backend.entity.UserEntity;
import com.ev_energy_management.backend.repository.LoginLogRepository;
import com.ev_energy_management.backend.repository.UserRepository;
import com.ev_energy_management.backend.util.MaskingUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LoginLogService {

    private final LoginLogRepository loginLogRepository;
    private final UserRepository userRepository;

    public LoginLogService(LoginLogRepository loginLogRepository, UserRepository userRepository) {
        this.loginLogRepository = loginLogRepository;
        this.userRepository = userRepository;
    }

    public List<LoginLogDto> findAll() {
        // 정렬 없이 findAll()만 쓰면 순서가 보장 안 돼서 방금 로그인한 게 최신순이 아니라
        // 아무데나 섞여 보였다 - 최신순(createdAt DESC)으로 고정.
        List<LoginLogEntity> entities = loginLogRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

        // 관리자 로그 화면에 이용자 UUID 대신 이름/role(=앱·관제·관리 구분용)을 보여주기 위해
        // 한 번에 조회(N+1 방지)
        List<UUID> userIds = entities.stream().map(LoginLogEntity::getUserId).distinct().toList();
        Map<UUID, UserEntity> usersById = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getUserId, u -> u));

        return entities.stream().map(entity -> toDto(entity, usersById)).toList();
    }

    public LoginLogDto findById(UUID logId) {
        LoginLogEntity entity = loginLogRepository.findById(logId)
                .orElseThrow(() -> new EntityNotFoundException("Login log not found: " + logId));
        UserEntity user = userRepository.findById(entity.getUserId()).orElse(null);
        return toDto(entity, user == null ? Map.of() : Map.of(entity.getUserId(), user));
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
        return findById(loginLogRepository.save(entity).getLogId());
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
        return findById(loginLogRepository.save(entity).getLogId());
    }

    public void delete(UUID logId) {
        loginLogRepository.deleteById(logId);
    }

    private LoginLogDto toDto(LoginLogEntity entity, Map<UUID, UserEntity> usersById) {
        UserEntity user = usersById.get(entity.getUserId());
        return new LoginLogDto(
                entity.getLogId(),
                MaskingUtils.maskIp(entity.getIpAddress()),
                entity.getUserAgent(),
                entity.getLocation(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUserId(),
                MaskingUtils.maskName(user == null ? null : user.getName()),
                user == null ? null : user.getRole(),
                entity.getFailReason()
        );
    }
}
