package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.entity.ActionLogEntity;
import com.ev_energy_management.backend.repository.ActionLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

// AuthService의 4개 호출 지점(SIGNUP/LOGIN/LOGOUT/PROFILE_UPDATE)만을 위한 얇은 기록 전용 서비스.
// 기존 ActionLogService/ActionLogController는 관리자 로그 조회 화면(LogManage.jsx)용 범용 CRUD로 그대로 둔다.
@Service
public class AuditLogService {

    private final ActionLogRepository actionLogRepository;
    // detail은 항상 이 서비스 내부에서 만든 단순 Map(String/List)만 직렬화하므로,
    // Spring이 관리하는 ObjectMapper(Jackson 2/3 버전 상황에 따라 타입이 갈릴 수 있음)에
    // 의존하지 않고 이 용도 전용 인스턴스를 직접 둔다.
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuditLogService(ActionLogRepository actionLogRepository) {
        this.actionLogRepository = actionLogRepository;
    }

    public void log(UUID userId, String actionType, String targetType, UUID targetId, Map<String, Object> detail) {
        String detailJson = null;
        if (detail != null) {
            try {
                detailJson = objectMapper.writeValueAsString(detail);
            } catch (Exception e) {
                detailJson = null;
            }
        }
        ActionLogEntity entity = ActionLogEntity.builder()
                .actionType(actionType)
                .targetType(targetType)
                .targetId(targetId)
                .detail(detailJson)
                .userId(userId)
                .build();
        actionLogRepository.save(entity);
    }
}
