package com.ev_energy_management.backend.repository;

import com.ev_energy_management.backend.entity.BatteryProposalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BatteryProposalRepository extends JpaRepository<BatteryProposalEntity, UUID> {
}
