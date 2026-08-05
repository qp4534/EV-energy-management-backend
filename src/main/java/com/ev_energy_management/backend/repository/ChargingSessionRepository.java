package com.ev_energy_management.backend.repository;

import com.ev_energy_management.backend.entity.ChargingSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChargingSessionRepository extends JpaRepository<ChargingSessionEntity, UUID> {
}
