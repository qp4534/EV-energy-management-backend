package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.DeviceTokenRegisterRequest;
import com.ev_energy_management.backend.entity.DeviceTokenEntity;
import com.ev_energy_management.backend.exception.InvalidRequestException;
import com.ev_energy_management.backend.repository.DeviceTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;

    public DeviceTokenService(DeviceTokenRepository deviceTokenRepository) {
        this.deviceTokenRepository = deviceTokenRepository;
    }

    // expo_push_token에 유니크 제약이 있어서, 같은 기기로 다른 계정에 로그인하면 그 계정으로
    // 재등록(소유자 교체)된다 - 기기당 여러 계정이 동시에 알림을 받는 상황을 막기 위함.
    @Transactional
    public void register(UUID userId, DeviceTokenRegisterRequest request) {
        if (request.expoPushToken() == null || request.expoPushToken().isBlank()) {
            throw new InvalidRequestException("expoPushToken이 필요합니다.");
        }
        DeviceTokenEntity entity = deviceTokenRepository.findByExpoPushToken(request.expoPushToken())
                .orElseGet(() -> DeviceTokenEntity.builder()
                        .expoPushToken(request.expoPushToken())
                        .build());
        entity.setUserId(userId);
        entity.setPlatform(request.platform());
        entity.setUpdatedAt(OffsetDateTime.now());
        deviceTokenRepository.save(entity);
    }

    // 로그아웃 시 이 기기로는 더 이상 알림이 안 가도록 호출한다(선택 - 안 불러도 다음 로그인 때
    // register가 다시 소유자를 갱신하므로 치명적이지는 않음).
    @Transactional
    public void unregister(String expoPushToken) {
        deviceTokenRepository.deleteByExpoPushToken(expoPushToken);
    }
}
