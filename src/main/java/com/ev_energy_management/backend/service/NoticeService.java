package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.NoticeDto;
import com.ev_energy_management.backend.dto.NotificationCreateRequest;
import com.ev_energy_management.backend.entity.NoticeEntity;
import com.ev_energy_management.backend.entity.UserEntity;
import com.ev_energy_management.backend.repository.NoticeRepository;
import com.ev_energy_management.backend.repository.UserRepository;
import com.ev_energy_management.backend.security.AuthenticatedUser;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class NoticeService {

    // 이용자(차주) 대상 공지사항이 만들어졌을 때 앱 알림으로 띄울 위험도. 실제 위험 등급이
    // 아니라 그냥 "정보성 알림"이라 EmergencyModal/ReportModal 같은 특수 팝업을 트리거하지
    // 않는 값을 쓴다.
    private static final String NOTICE_NOTIFICATION_RISK_LEVEL = "정상";

    private final NoticeRepository noticeRepository;
    private final ActionLogWriter actionLogWriter;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public NoticeService(
            NoticeRepository noticeRepository,
            ActionLogWriter actionLogWriter,
            UserRepository userRepository,
            NotificationService notificationService
    ) {
        this.noticeRepository = noticeRepository;
        this.actionLogWriter = actionLogWriter;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
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
        notifyCarOwnersIfTargeted(saved);
        return saved;
    }

    // 관리자/관제자 대상 공지(target_role: ADMIN/CONTROLLER)는 그 화면들이 자체적으로 목록을
    // 조회해서 보여주므로 별도 알림이 필요 없다. "이용자"를 대상으로 지정한 공지만 차주 앱에
    // 실제로 노출되는 경로(NOTIFICATIONS)가 없어서, 여기서 전체 차주에게 알림을 만들어준다.
    private void notifyCarOwnersIfTargeted(NoticeDto notice) {
        if (!"이용자".equals(notice.targetRole())) return;

        for (UserEntity owner : userRepository.findByRoleAndIsDeletedFalse("이용자")) {
            notificationService.create(
                    owner.getUserId(),
                    new NotificationCreateRequest(
                            NOTICE_NOTIFICATION_RISK_LEVEL,
                            notice.title(),
                            notice.content(),
                            null,
                            null
                    )
            );
        }
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