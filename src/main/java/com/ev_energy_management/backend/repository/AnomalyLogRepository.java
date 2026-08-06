package com.ev_energy_management.backend.repository;

import com.ev_energy_management.backend.entity.AnomalyLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AnomalyLogRepository extends JpaRepository<AnomalyLogEntity, UUID> {
}
