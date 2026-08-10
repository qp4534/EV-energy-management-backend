package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.NotificationMatrixDto;
import com.ev_energy_management.backend.entity.NotificationMatrixEntity;
import com.ev_energy_management.backend.repository.NotificationMatrixRepository;
import com.ev_energy_management.backend.security.AuthenticatedUser;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class NotificationMatrixService {

    private final NotificationMatrixRepository notificationMatrixRepository;
    private final ActionLogWriter actionLogWriter;

    public NotificationMatrixService(NotificationMatrixRepository notificationMatrixRepository,
                                     ActionLogWriter actionLogWriter) {
        this.notificationMatrixRepository = notificationMatrixRepository;
        this.actionLogWriter = actionLogWriter;
    }

    public List<NotificationMatrixDto> findAll() {
        return notificationMatrixRepository.findAll().stream().map(this::toDto).toList();
    }

    public NotificationMatrixDto findById(UUID matrixId) {
        return toDto(notificationMatrixRepository.findById(matrixId)
                .orElseThrow(() -> new EntityNotFoundException("Notification matrix not found: " + matrixId)));
    }

    public NotificationMatrixDto create(NotificationMatrixDto request) {
        NotificationMatrixEntity entity = NotificationMatrixEntity.builder()
                .riskLevel(request.riskLevel() != null ? request.riskLevel() : "정상")
                .isEnabled(request.isEnabled())
                .channelId(request.channelId())
                .build();
        return toDto(notificationMatrixRepository.save(entity));
    }

    public NotificationMatrixDto update(AuthenticatedUser actor, UUID matrixId, NotificationMatrixDto request) {
        NotificationMatrixEntity entity = notificationMatrixRepository.findById(matrixId)
                .orElseThrow(() -> new EntityNotFoundException("Notification matrix not found: " + matrixId));
        entity.setRiskLevel(request.riskLevel());
        entity.setIsEnabled(request.isEnabled());
        entity.setChannelId(request.channelId());
        NotificationMatrixDto saved = toDto(notificationMatrixRepository.save(entity));

        actionLogWriter.write(
                actor == null ? null : actor.userId(),
                "NOTIFICATION_MATRIX_UPDATE",
                "NOTIFICATION_MATRIX",
                matrixId,
                Map.of(
                        "riskLevel", request.riskLevel() == null ? "" : request.riskLevel(),
                        "channelId", request.channelId() == null ? "" : request.channelId(),
                        "isEnabled", String.valueOf(request.isEnabled())
                )
        );
        return saved;
    }

    public void delete(UUID matrixId) {
        notificationMatrixRepository.deleteById(matrixId);
    }

    private NotificationMatrixDto toDto(NotificationMatrixEntity entity) {
        return new NotificationMatrixDto(
                entity.getMatrixId(),
                entity.getRiskLevel(),
                entity.getIsEnabled(),
                entity.getChannelId()
        );
    }
}