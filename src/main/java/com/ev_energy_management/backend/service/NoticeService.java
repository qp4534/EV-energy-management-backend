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

    // 이용자(차주)는 본인이 볼 수 있는 공지(전체 또는 target_role=USER)만, 관제자/관리자는
    // 공지 관리 화면에서 전체를 다뤄야 하니 그대로 다 본다.
    public List<NoticeDto> findAll(AuthenticatedUser user) {
        List<NoticeEntity> notices = noticeRepository.findAll();
        if ("이용자".equals(user.role())) {
            notices = notices.stream().filter(this::visibleToCarOwners).toList();
        }
        return notices.stream().map(this::toDto).toList();
    }

    public NoticeDto findById(AuthenticatedUser user, UUID noticeId) {
        NoticeEntity entity = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("Notice not found: " + noticeId));
        // 이용자가 관리자/관제자 전용 공지 id를 직접 조회하려 하면, 그런 공지가 존재하는지 자체를
        // 드러내지 않기 위해 접근 거부 대신 "찾을 수 없음"으로 응답한다.
        if ("이용자".equals(user.role()) && !visibleToCarOwners(entity)) {
            throw new EntityNotFoundException("Notice not found: " + noticeId);
        }
        return toDto(entity);
    }

    private boolean visibleToCarOwners(NoticeEntity notice) {
        String targetRole = notice.getTargetRole();
        return targetRole == null || "USER".equals(targetRole);
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

    // 관리자/관제자만 대상으로 하는 공지(target_role: ADMIN/CONTROLLER)는 그 화면들이 자체적으로
    // 목록을 조회해서 보여주므로 차주에게는 필요 없다. "USER"(이용자/차주 전용)이거나
    // targetRole이 null인 "전체" 공지는 차주도 봐야 하는데, 차주 앱엔 이게 노출되는 경로
    // (NOTIFICATIONS)가 없어서 여기서 전체 차주에게 알림을 만들어준다.
    // target_role은 frontend-web(NoticeWrite/NoticeEdit)에서 ADMIN/CONTROLLER/USER 영문 값
    // 또는 "전체"일 때 null로 변환해서 보낸다 - DB 자체엔 CHECK 제약이 없지만 이 표기가 이미
    // 통용되는 컨벤션이다.
    private void notifyCarOwnersIfTargeted(NoticeDto notice) {
        String targetRole = notice.targetRole();
        boolean staffOnly = "ADMIN".equals(targetRole) || "CONTROLLER".equals(targetRole);
        if (staffOnly) return;

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