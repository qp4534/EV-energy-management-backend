package com.ev_energy_management.backend.repository;

import com.ev_energy_management.backend.entity.ChargingStationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChargingStationRepository extends JpaRepository<ChargingStationEntity, UUID> {
}
