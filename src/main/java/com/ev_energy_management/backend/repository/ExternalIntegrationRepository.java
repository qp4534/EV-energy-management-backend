package com.ev_energy_management.backend.repository;

import com.ev_energy_management.backend.entity.ExternalIntegrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExternalIntegrationRepository extends JpaRepository<ExternalIntegrationEntity, UUID> {
}
