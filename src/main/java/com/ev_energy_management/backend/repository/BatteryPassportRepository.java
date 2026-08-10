package com.ev_energy_management.backend.repository;

import com.ev_energy_management.backend.entity.BatteryPassportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BatteryPassportRepository extends JpaRepository<BatteryPassportEntity, UUID> {
    Optional<BatteryPassportEntity> findByCarId(UUID carId);
}
