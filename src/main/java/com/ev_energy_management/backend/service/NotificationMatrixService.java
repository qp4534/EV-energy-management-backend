package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.NotificationMatrixDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationMatrixService {

    private List<NotificationMatrixDto> mockData() {
        return List.of(
                new NotificationMatrixDto(UUID.randomUUID(), "긴급", UUID.randomUUID(), UUID.randomUUID()),
                new NotificationMatrixDto(UUID.randomUUID(), "주의", UUID.randomUUID(), UUID.randomUUID())
        );
    }

    public List<NotificationMatrixDto> findAll() {
        return mockData();
    }

    public NotificationMatrixDto findById(UUID matrixId) {
        return new NotificationMatrixDto(matrixId, "긴급", UUID.randomUUID(), UUID.randomUUID());
    }

    public NotificationMatrixDto create(NotificationMatrixDto request) {
        return new NotificationMatrixDto(UUID.randomUUID(), request.riskLevel(), request.isEnabled(), request.channelId());
    }

    public NotificationMatrixDto update(UUID matrixId, NotificationMatrixDto request) {
        return new NotificationMatrixDto(matrixId, request.riskLevel(), request.isEnabled(), request.channelId());
    }

    public void delete(UUID matrixId) {
        // TODO: ERD 확정 후 실제 삭제 로직 연결
    }
}
