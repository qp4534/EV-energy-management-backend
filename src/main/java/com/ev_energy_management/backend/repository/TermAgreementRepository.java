package com.ev_energy_management.backend.repository;

import com.ev_energy_management.backend.entity.TermAgreementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TermAgreementRepository extends JpaRepository<TermAgreementEntity, UUID> {
}
