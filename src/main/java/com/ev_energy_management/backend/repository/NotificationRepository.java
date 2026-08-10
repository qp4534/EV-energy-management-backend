package com.ev_energy_management.backend.repository;

import com.ev_energy_management.backend.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {
    List<NotificationEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<NotificationEntity> findByNotificationIdAndUserId(UUID notificationId, UUID userId);
}
