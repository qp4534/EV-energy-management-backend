package com.ev_energy_management.backend.repository;

import com.ev_energy_management.backend.entity.BatchJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchJobRepository extends JpaRepository<BatchJobEntity, String> {
}
