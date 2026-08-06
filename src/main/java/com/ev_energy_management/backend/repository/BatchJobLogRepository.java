package com.ev_energy_management.backend.repository;

import com.ev_energy_management.backend.entity.BatchJobLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BatchJobLogRepository extends JpaRepository<BatchJobLogEntity, UUID> {
}
