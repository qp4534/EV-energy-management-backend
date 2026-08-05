package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.ActionLogDto;
import com.ev_energy_management.backend.entity.ActionLogEntity;
import com.ev_energy_management.backend.repository.ActionLogRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ActionLogService {

    private final ActionLogRepository actionLogRepository;

    public ActionLogService(ActionLogRepository actionLogRepository) {
        this.actionLogRepository = actionLogRepository;
    }

    public List<ActionLogDto> findAll() {
        return actionLogRepository.findAll().stream().map(this::toDto).toList();
    }

    public ActionLogDto findById(UUID actionId) {
        return toDto(actionLogRepository.findById(actionId)
                .orElseThrow(() -> new EntityNotFoundException("Action log not found: " + actionId)));
    }

    public ActionLogDto create(ActionLogDto request) {
        ActionLogEntity entity = ActionLogEntity.builder()
                .actionType(request.actionType())
                .targetType(request.targetType())
                .targetId(request.targetId())
                .detail(request.detail())
                .userId(request.userId())
                .build();
        return toDto(actionLogRepository.save(entity));
    }

    public ActionLogDto update(UUID actionId, ActionLogDto request) {
        ActionLogEntity entity = actionLogRepository.findById(actionId)
                .orElseThrow(() -> new EntityNotFoundException("Action log not found: " + actionId));
        entity.setActionType(request.actionType());
        entity.setTargetType(request.targetType());
        entity.setTargetId(request.targetId());
        entity.setDetail(request.detail());
        entity.setUserId(request.userId());
        return toDto(actionLogRepository.save(entity));
    }

    public void delete(UUID actionId) {
        actionLogRepository.deleteById(actionId);
    }

    private ActionLogDto toDto(ActionLogEntity entity) {
        return new ActionLogDto(
                entity.getActionId(),
                entity.getActionType(),
                entity.getTargetType(),
                entity.getTargetId(),
                entity.getDetail(),
                entity.getCreatedAt(),
                entity.getUserId()
        );
    }
}
