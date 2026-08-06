package com.ev_energy_management.backend.repository;

import com.ev_energy_management.backend.entity.BatteryOfferEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BatteryOfferRepository extends JpaRepository<BatteryOfferEntity, UUID> {
}
