package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.NotificationCreateRequest;
import com.ev_energy_management.backend.dto.NotificationDto;
import com.ev_energy_management.backend.entity.NotificationEntity;
import com.ev_energy_management.backend.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
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
        return toDto(notificationRepository.save(entity));
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
