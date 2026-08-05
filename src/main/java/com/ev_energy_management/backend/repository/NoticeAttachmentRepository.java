package com.ev_energy_management.backend.repository;

import com.ev_energy_management.backend.entity.NoticeAttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NoticeAttachmentRepository extends JpaRepository<NoticeAttachmentEntity, UUID> {
}
