package com.ev_energy_management.backend.repository;

import com.ev_energy_management.backend.entity.NotificationMatrixEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationMatrixRepository extends JpaRepository<NotificationMatrixEntity, UUID> {
}
