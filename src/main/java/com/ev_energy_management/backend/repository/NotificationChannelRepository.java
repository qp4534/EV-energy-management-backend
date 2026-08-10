package com.ev_energy_management.backend.repository;

import com.ev_energy_management.backend.entity.NotificationChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationChannelRepository extends JpaRepository<NotificationChannelEntity, String> {
}
