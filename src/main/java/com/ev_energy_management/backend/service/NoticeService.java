package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.NoticeDto;
import com.ev_energy_management.backend.entity.NoticeEntity;
import com.ev_energy_management.backend.repository.NoticeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public NoticeService(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    public List<NoticeDto> findAll() {
        return noticeRepository.findAll().stream().map(this::toDto).toList();
    }

    public NoticeDto findById(UUID noticeId) {
        return toDto(noticeRepository.findById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("Notice not found: " + noticeId)));
    }

    public NoticeDto create(NoticeDto request) {
        NoticeEntity entity = NoticeEntity.builder()
                .title(request.title())
                .content(request.content())
                .isPinned(request.isPinned() != null ? request.isPinned() : false)
                .userId(request.userId())
                .isRead(false)
                .isImportant(request.isImportant() != null ? request.isImportant() : false)
                .targetRole(request.targetRole())
                .viewCount(0)
                .build();
        return toDto(noticeRepository.save(entity));
    }

    public NoticeDto update(UUID noticeId, NoticeDto request) {
        NoticeEntity entity = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("Notice not found: " + noticeId));
        entity.setTitle(request.title());
        entity.setContent(request.content());
        entity.setIsPinned(request.isPinned());
        entity.setUserId(request.userId());
        entity.setIsRead(request.isRead());
        entity.setIsImportant(request.isImportant());
        entity.setTargetRole(request.targetRole());
        entity.setViewCount(request.viewCount());
        entity.setUpdatedAt(OffsetDateTime.now());
        return toDto(noticeRepository.save(entity));
    }

    public void delete(UUID noticeId) {
        noticeRepository.deleteById(noticeId);
    }

    private NoticeDto toDto(NoticeEntity entity) {
        return new NoticeDto(
                entity.getNoticeId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getIsPinned(),
                entity.getCreatedAt(),
                entity.getUserId(),
                entity.getIsRead(),
                entity.getIsImportant(),
                entity.getTargetRole(),
                entity.getViewCount(),
                entity.getUpdatedAt()
        );
    }
}
