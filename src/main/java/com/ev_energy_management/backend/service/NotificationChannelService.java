package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.NotificationChannelDto;
import com.ev_energy_management.backend.entity.NotificationChannelEntity;
import com.ev_energy_management.backend.repository.NotificationChannelRepository;
import com.ev_energy_management.backend.security.AuthenticatedUser;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class NotificationChannelService {

    private final NotificationChannelRepository notificationChannelRepository;
    private final ActionLogWriter actionLogWriter;

    public NotificationChannelService(NotificationChannelRepository notificationChannelRepository,
                                      ActionLogWriter actionLogWriter) {
        this.notificationChannelRepository = notificationChannelRepository;
        this.actionLogWriter = actionLogWriter;
    }

    public List<NotificationChannelDto> findAll() {
        return notificationChannelRepository.findAll().stream().map(this::toDto).toList();
    }

    public NotificationChannelDto findById(String channelId) {
        return toDto(notificationChannelRepository.findById(channelId)
                .orElseThrow(() -> new EntityNotFoundException("Notification channel not found: " + channelId)));
    }

    public NotificationChannelDto create(NotificationChannelDto request) {
        NotificationChannelEntity entity = NotificationChannelEntity.builder()
                .channelId(request.channelId()) // 더 이상 자동 생성 안 됨 (business key), 반드시 지정 필요
                .channelName(request.channelName())
                .isActive(request.isActive() != null ? request.isActive() : true)
                .build();
        return toDto(notificationChannelRepository.save(entity));
    }

    public NotificationChannelDto update(AuthenticatedUser actor, String channelId, NotificationChannelDto request) {
        NotificationChannelEntity entity = notificationChannelRepository.findById(channelId)
                .orElseThrow(() -> new EntityNotFoundException("Notification channel not found: " + channelId));
        entity.setChannelName(request.channelName());
        entity.setIsActive(request.isActive());
        NotificationChannelDto saved = toDto(notificationChannelRepository.save(entity));

        actionLogWriter.write(
                actor == null ? null : actor.userId(),
                "NOTIFICATION_CHANNEL_UPDATE",
                "NOTIFICATION_CHANNEL",
                // targetId가 UUID 타입인데 channelId는 문자열(business key)이라 그대로 못 넣음
                // -> 문자열에서 고정된 UUID를 만들어서 사용, 원본 값은 detail에 같이 남김
                java.util.UUID.nameUUIDFromBytes(channelId.getBytes()),
                Map.of("channelId", channelId, "isActive", String.valueOf(request.isActive()))
        );
        return saved;
    }

    public void delete(String channelId) {
        notificationChannelRepository.deleteById(channelId);
    }

    private NotificationChannelDto toDto(NotificationChannelEntity entity) {
        return new NotificationChannelDto(
                entity.getChannelId(),
                entity.getChannelName(),
                entity.getIsActive(),
                entity.getUpdatedAt()
        );
    }
}