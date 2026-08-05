package com.ev_energy_management.backend.repository;

import com.ev_energy_management.backend.entity.NoticeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NoticeRepository extends JpaRepository<NoticeEntity, UUID> {
}
