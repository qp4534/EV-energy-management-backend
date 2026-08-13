package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.NoticeDto;
import com.ev_energy_management.backend.entity.NoticeEntity;
import com.ev_energy_management.backend.repository.NoticeRepository;
import com.ev_energy_management.backend.security.AuthenticatedUser;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final ActionLogWriter actionLogWriter;
    private final NoticeAttachmentService noticeAttachmentService;

    public NoticeService(NoticeRepository noticeRepository, ActionLogWriter actionLogWriter,
                         NoticeAttachmentService noticeAttachmentService) {
        this.noticeRepository = noticeRepository;
        this.actionLogWriter = actionLogWriter;
        this.noticeAttachmentService = noticeAttachmentService;
    }

    public List<NoticeDto> findAll() {
        return noticeRepository.findAll().stream().map(this::toDto).toList();
    }

    public NoticeDto findById(UUID noticeId) {
        return toDto(noticeRepository.findById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("Notice not found: " + noticeId)));
    }

    public NoticeDto create(AuthenticatedUser actor, NoticeDto request) {
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
        NoticeDto saved = toDto(noticeRepository.save(entity));

        actionLogWriter.write(
                actor == null ? null : actor.userId(),
                "NOTICE_CREATE",
                "NOTICE",
                saved.noticeId(),
                Map.of("title", request.title() == null ? "" : request.title())
        );
        return saved;
    }

    public NoticeDto update(AuthenticatedUser actor, UUID noticeId, NoticeDto request) {
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
        NoticeDto saved = toDto(noticeRepository.save(entity));

        actionLogWriter.write(
                actor == null ? null : actor.userId(),
                "NOTICE_UPDATE",
                "NOTICE",
                noticeId,
                Map.of("title", request.title() == null ? "" : request.title())
        );
        return saved;
    }

    public void delete(AuthenticatedUser actor, UUID noticeId) {
        noticeAttachmentService.deleteByNoticeId(noticeId); // 공지 지우기 전에 딸린 첨부파일부터 정리
        noticeRepository.deleteById(noticeId);
        actionLogWriter.write(
                actor == null ? null : actor.userId(),
                "NOTICE_DELETE",
                "NOTICE",
                noticeId,
                Map.of()
        );
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