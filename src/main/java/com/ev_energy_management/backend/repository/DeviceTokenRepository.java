package com.ev_energy_management.backend.repository;

import com.ev_energy_management.backend.entity.DeviceTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceTokenRepository extends JpaRepository<DeviceTokenEntity, UUID> {
    Optional<DeviceTokenEntity> findByExpoPushToken(String expoPushToken);

    List<DeviceTokenEntity> findByUserId(UUID userId);

    void deleteByExpoPushToken(String expoPushToken);
}
