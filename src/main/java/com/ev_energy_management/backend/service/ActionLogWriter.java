package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.entity.ActionLogEntity;
import com.ev_energy_management.backend.repository.ActionLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

// "관리자 작업" 로그(ACTION_LOGS)를 남기는 공용 헬퍼
@Service
public class ActionLogWriter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ActionLogRepository actionLogRepository;

    public ActionLogWriter(ActionLogRepository actionLogRepository) {
        this.actionLogRepository = actionLogRepository;
    }

    public void write(UUID actorUserId, String actionType, String targetType, UUID targetId, Map<String, Object> detail) {
        if (actorUserId == null) return; // 인증 정보가 없는 상황이면 로그 없이 조용히 무시 (기능 자체를 막지 않기 위함)

        String detailJson;
        try {
            detailJson = OBJECT_MAPPER.writeValueAsString(detail == null ? Map.of() : detail);
        } catch (Exception e) {
            detailJson = "{}";
        }

        ActionLogEntity entity = ActionLogEntity.builder()
                .actionType(actionType)
                .targetType(targetType)
                .targetId(targetId)
                .detail(detailJson)
                .userId(actorUserId)
                .build();
        actionLogRepository.save(entity);
    }
}