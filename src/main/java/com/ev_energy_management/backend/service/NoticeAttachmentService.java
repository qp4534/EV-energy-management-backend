package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.NoticeAttachmentDto;
import com.ev_energy_management.backend.entity.NoticeAttachmentEntity;
import com.ev_energy_management.backend.repository.NoticeAttachmentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NoticeAttachmentService {

    private final NoticeAttachmentRepository noticeAttachmentRepository;

    public NoticeAttachmentService(NoticeAttachmentRepository noticeAttachmentRepository) {
        this.noticeAttachmentRepository = noticeAttachmentRepository;
    }

    public List<NoticeAttachmentDto> findAll() {
        return noticeAttachmentRepository.findAll().stream().map(this::toDto).toList();
    }

    public NoticeAttachmentDto findById(UUID attachmentId) {
        return toDto(noticeAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new EntityNotFoundException("Notice attachment not found: " + attachmentId)));
    }

    public NoticeAttachmentDto create(NoticeAttachmentDto request) {
        NoticeAttachmentEntity entity = NoticeAttachmentEntity.builder()
                .fileName(request.fileName())
                .fileUrl(request.fileUrl())
                .fileSize(request.fileSize())
                .fileType(request.fileType())
                .noticeId(request.noticeId())
                .build();
        return toDto(noticeAttachmentRepository.save(entity));
    }

    public NoticeAttachmentDto update(UUID attachmentId, NoticeAttachmentDto request) {
        NoticeAttachmentEntity entity = noticeAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new EntityNotFoundException("Notice attachment not found: " + attachmentId));
        entity.setFileName(request.fileName());
        entity.setFileUrl(request.fileUrl());
        entity.setFileSize(request.fileSize());
        entity.setFileType(request.fileType());
        entity.setNoticeId(request.noticeId());
        return toDto(noticeAttachmentRepository.save(entity));
    }

    public void delete(UUID attachmentId) {
        noticeAttachmentRepository.deleteById(attachmentId);
    }

    private NoticeAttachmentDto toDto(NoticeAttachmentEntity entity) {
        return new NoticeAttachmentDto(
                entity.getAttachmentId(),
                entity.getFileName(),
                entity.getFileUrl(),
                entity.getFileSize(),
                entity.getFileType(),
                entity.getNoticeId()
        );
    }
}
