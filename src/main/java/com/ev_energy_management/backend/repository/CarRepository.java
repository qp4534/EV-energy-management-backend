package com.ev_energy_management.backend.repository;

import com.ev_energy_management.backend.entity.CarEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CarRepository extends JpaRepository<CarEntity, UUID> {
    boolean existsByCarIdAndUserId(UUID carId, UUID userId);
    List<CarEntity> findByUserId(UUID userId);
}
