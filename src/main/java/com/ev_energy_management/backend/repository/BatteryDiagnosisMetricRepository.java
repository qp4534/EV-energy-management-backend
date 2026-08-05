package com.ev_energy_management.backend.repository;

import com.ev_energy_management.backend.entity.BatteryDiagnosisMetricEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BatteryDiagnosisMetricRepository extends JpaRepository<BatteryDiagnosisMetricEntity, UUID> {
}
