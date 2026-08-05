package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.AnomalyLogDto;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AnomalyLogService {

    private List<AnomalyLogDto> mockData() {
        return List.of(
                new AnomalyLogDto(UUID.randomUUID(), "온도 상승", "THERMAL_SENSOR", "62.3",
                        OffsetDateTime.now(), "경고", UUID.randomUUID(), UUID.randomUUID()),
                new AnomalyLogDto(UUID.randomUUID(), "정상", "BATTERY_SENSOR", "24.1",
                        OffsetDateTime.now(), "정상", UUID.randomUUID(), UUID.randomUUID())
        );
    }

    public List<AnomalyLogDto> findAll() {
        return mockData();
    }

    public AnomalyLogDto findById(UUID anomalyId) {
        return new AnomalyLogDto(anomalyId, "온도 상승", "THERMAL_SENSOR", "62.3",
                OffsetDateTime.now(), "경고", UUID.randomUUID(), UUID.randomUUID());
    }

    public AnomalyLogDto create(AnomalyLogDto request) {
        return new AnomalyLogDto(UUID.randomUUID(), request.abnormalType(), request.sourceType(),
                request.triggerValue(), OffsetDateTime.now(), request.riskLevel(), request.carId(), request.sessionId());
    }

    public AnomalyLogDto update(UUID anomalyId, AnomalyLogDto request) {
        return new AnomalyLogDto(anomalyId, request.abnormalType(), request.sourceType(),
                request.triggerValue(), request.detectedAt(), request.riskLevel(), request.carId(), request.sessionId());
    }

    public void delete(UUID anomalyId) {
        // TODO: ERD 확정 후 실제 삭제 로직 연결
    }
}
