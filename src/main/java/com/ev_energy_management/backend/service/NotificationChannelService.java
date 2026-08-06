package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.NotificationChannelDto;
import com.ev_energy_management.backend.entity.NotificationChannelEntity;
import com.ev_energy_management.backend.repository.NotificationChannelRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationChannelService {

    private final NotificationChannelRepository notificationChannelRepository;

    public NotificationChannelService(NotificationChannelRepository notificationChannelRepository) {
        this.notificationChannelRepository = notificationChannelRepository;
    }

    public List<NotificationChannelDto> findAll() {
        return notificationChannelRepository.findAll().stream().map(this::toDto).toList();
    }

    public NotificationChannelDto findById(UUID channelId) {
        return toDto(notificationChannelRepository.findById(channelId)
                .orElseThrow(() -> new EntityNotFoundException("Notification channel not found: " + channelId)));
    }

    public NotificationChannelDto create(NotificationChannelDto request) {
        NotificationChannelEntity entity = NotificationChannelEntity.builder()
                .channelName(request.channelName())
                .isActive(request.isActive() != null ? request.isActive() : true)
                .build();
        return toDto(notificationChannelRepository.save(entity));
    }

    public NotificationChannelDto update(UUID channelId, NotificationChannelDto request) {
        NotificationChannelEntity entity = notificationChannelRepository.findById(channelId)
                .orElseThrow(() -> new EntityNotFoundException("Notification channel not found: " + channelId));
        entity.setChannelName(request.channelName());
        entity.setIsActive(request.isActive());
        return toDto(notificationChannelRepository.save(entity));
    }

    public void delete(UUID channelId) {
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
