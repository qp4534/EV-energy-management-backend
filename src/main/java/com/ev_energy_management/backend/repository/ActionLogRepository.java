package com.ev_energy_management.backend.repository;

import com.ev_energy_management.backend.entity.ActionLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ActionLogRepository extends JpaRepository<ActionLogEntity, UUID> {
}
