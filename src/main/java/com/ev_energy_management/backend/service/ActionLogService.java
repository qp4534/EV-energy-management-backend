package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.ActionLogDto;
import com.ev_energy_management.backend.entity.ActionLogEntity;
import com.ev_energy_management.backend.entity.UserEntity;
import com.ev_energy_management.backend.repository.ActionLogRepository;
import com.ev_energy_management.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ActionLogService {

    private final ActionLogRepository actionLogRepository;
    private final UserRepository userRepository;

    public ActionLogService(ActionLogRepository actionLogRepository, UserRepository userRepository) {
        this.actionLogRepository = actionLogRepository;
        this.userRepository = userRepository;
    }

    public List<ActionLogDto> findAll() {
        // 정렬 없이 findAll()만 쓰면 순서가 보장 안 돼서 방금 한 액션이 최신순이 아니라
        // 아무데나 섞여 보였다 - 최신순(createdAt DESC)으로 고정.
        List<ActionLogEntity> entities = actionLogRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

        // 관리자 로그 화면에 이용자/관리자 UUID 대신 이름을 보여주기 위해 한 번에 조회(N+1 방지)
        List<UUID> userIds = entities.stream().map(ActionLogEntity::getUserId).distinct().toList();
        Map<UUID, String> userNamesById = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getUserId, UserEntity::getName));

        return entities.stream().map(entity -> toDto(entity, userNamesById)).toList();
    }

    public ActionLogDto findById(UUID actionId) {
        ActionLogEntity entity = actionLogRepository.findById(actionId)
                .orElseThrow(() -> new EntityNotFoundException("Action log not found: " + actionId));
        String userName = userRepository.findById(entity.getUserId()).map(UserEntity::getName).orElse(null);
        return toDto(entity, Map.of(entity.getUserId(), userName == null ? "" : userName));
    }

    public ActionLogDto create(ActionLogDto request) {
        ActionLogEntity entity = ActionLogEntity.builder()
                .actionType(request.actionType())
                .targetType(request.targetType())
                .targetId(request.targetId())
                .detail(request.detail())
                .userId(request.userId())
                .build();
        return findById(actionLogRepository.save(entity).getActionId());
    }

    public ActionLogDto update(UUID actionId, ActionLogDto request) {
        ActionLogEntity entity = actionLogRepository.findById(actionId)
                .orElseThrow(() -> new EntityNotFoundException("Action log not found: " + actionId));
        entity.setActionType(request.actionType());
        entity.setTargetType(request.targetType());
        entity.setTargetId(request.targetId());
        entity.setDetail(request.detail());
        entity.setUserId(request.userId());
        return findById(actionLogRepository.save(entity).getActionId());
    }

    public void delete(UUID actionId) {
        actionLogRepository.deleteById(actionId);
    }

    private ActionLogDto toDto(ActionLogEntity entity, Map<UUID, String> userNamesById) {
        return new ActionLogDto(
                entity.getActionId(),
                entity.getActionType(),
                entity.getTargetType(),
                entity.getTargetId(),
                entity.getDetail(),
                entity.getCreatedAt(),
                entity.getUserId(),
                userNamesById.get(entity.getUserId())
        );
    }
}
