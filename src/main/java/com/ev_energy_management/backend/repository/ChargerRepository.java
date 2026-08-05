package com.ev_energy_management.backend.repository;

import com.ev_energy_management.backend.entity.ChargerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChargerRepository extends JpaRepository<ChargerEntity, UUID> {
}
