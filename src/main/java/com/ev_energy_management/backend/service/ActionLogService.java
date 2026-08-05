package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.ActionLogDto;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ActionLogService {

    private List<ActionLogDto> mockData() {
        return List.of(
                new ActionLogDto(UUID.randomUUID(), "LOGIN", "USER", UUID.randomUUID(),
                        "{\"result\":\"success\"}", OffsetDateTime.now(), UUID.randomUUID()),
                new ActionLogDto(UUID.randomUUID(), "NOTICE_CREATE", "NOTICE", UUID.randomUUID(),
                        "{\"title\":\"정기 점검 안내\"}", OffsetDateTime.now(), UUID.randomUUID())
        );
    }

    public List<ActionLogDto> findAll() {
        return mockData();
    }

    public ActionLogDto findById(UUID actionId) {
        return new ActionLogDto(actionId, "LOGIN", "USER", UUID.randomUUID(),
                "{\"result\":\"success\"}", OffsetDateTime.now(), UUID.randomUUID());
    }

    public ActionLogDto create(ActionLogDto request) {
        return new ActionLogDto(UUID.randomUUID(), request.actionType(), request.targetType(), request.targetId(),
                request.detail(), OffsetDateTime.now(), request.userId());
    }

    public ActionLogDto update(UUID actionId, ActionLogDto request) {
        return new ActionLogDto(actionId, request.actionType(), request.targetType(), request.targetId(),
                request.detail(), request.createdAt(), request.userId());
    }

    public void delete(UUID actionId) {
        // TODO: ERD 확정 후 실제 삭제 로직 연결
    }
}
