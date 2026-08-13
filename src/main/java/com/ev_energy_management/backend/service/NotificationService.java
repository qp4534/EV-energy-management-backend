package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.client.ExpoPushClient;
import com.ev_energy_management.backend.dto.NotificationCreateRequest;
import com.ev_energy_management.backend.dto.NotificationDto;
import com.ev_energy_management.backend.entity.DeviceTokenEntity;
import com.ev_energy_management.backend.entity.NotificationEntity;
import com.ev_energy_management.backend.entity.UserEntity;
import com.ev_energy_management.backend.repository.DeviceTokenRepository;
import com.ev_energy_management.backend.repository.NotificationRepository;
import com.ev_energy_management.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final ExpoPushClient expoPushClient;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            DeviceTokenRepository deviceTokenRepository,
            ExpoPushClient expoPushClient
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.expoPushClient = expoPushClient;
    }

    // 다른 도메인(cars, ai-reports 등)과 달리 이 테이블은 user_id를 갖고 있어서, 처음부터
    // 로그인한 본인 알림만 보이도록 서버에서 걸러낸다.
    public List<NotificationDto> findMine(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDto)
                .toList();
    }

    public NotificationDto findMineById(UUID userId, UUID notificationId) {
        return toDto(findOwned(userId, notificationId));
    }

    public NotificationDto create(UUID userId, NotificationCreateRequest request) {
        NotificationEntity entity = NotificationEntity.builder()
                .riskLevel(request.riskLevel())
                .title(request.title())
                .body(request.body())
                .userId(userId)
                .carId(request.carId())
                .reportId(request.reportId())
                .isRead(false)
                .build();
        NotificationDto saved = toDto(notificationRepository.save(entity));
        sendPush(userId, saved);
        return saved;
    }

    // 앱을 안 켜둔 상태에서도 실제로 기기가 울리게 하는 부분. NOTIFICATIONS row 생성(인앱
    // 알림함/배지)은 이미 끝났으니, 이건 실패해도 create() 자체를 실패시키지 않는다 - 사용자가
    // 알림 설정을 껐거나(pushEnabled=false) 등록된 기기가 없으면 그냥 조용히 건너뛴다.
    private void sendPush(UUID userId, NotificationDto notification) {
        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null || !Boolean.TRUE.equals(user.getPushEnabled())) return;

        List<String> tokens = deviceTokenRepository.findByUserId(userId).stream()
                .map(DeviceTokenEntity::getExpoPushToken)
                .toList();
        if (tokens.isEmpty()) return;

        expoPushClient.send(
                tokens,
                notification.title(),
                notification.body(),
                Map.of("notificationId", notification.notificationId().toString())
        );
    }

    @Transactional
    public NotificationDto markAsRead(UUID userId, UUID notificationId) {
        NotificationEntity entity = findOwned(userId, notificationId);
        if (!Boolean.TRUE.equals(entity.getIsRead())) {
            entity.setIsRead(true);
            entity = notificationRepository.save(entity);
        }
        return toDto(entity);
    }

    private NotificationEntity findOwned(UUID userId, UUID notificationId) {
        return notificationRepository.findByNotificationIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found: " + notificationId));
    }

    private NotificationDto toDto(NotificationEntity entity) {
        return new NotificationDto(
                entity.getNotificationId(),
                entity.getRiskLevel(),
                entity.getTitle(),
                entity.getBody(),
                entity.getIsRead(),
                entity.getCreatedAt(),
                entity.getUserId(),
                entity.getCarId(),
                entity.getReportId()
        );
    }
}
