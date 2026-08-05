package com.ev_energy_management.backend.repository;

import com.ev_energy_management.backend.entity.TwinFrameEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TwinFrameRepository extends JpaRepository<TwinFrameEntity, UUID> {

    List<TwinFrameEntity> findByCarIdOrderByObservedAtDesc(UUID carId);
}
