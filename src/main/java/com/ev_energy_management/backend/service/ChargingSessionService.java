package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.ChargingSessionDto;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ChargingSessionService {

    private List<ChargingSessionDto> mockData() {
        return List.of(
                new ChargingSessionDto(UUID.randomUUID(), OffsetDateTime.now().minusHours(2), OffsetDateTime.now().minusHours(1),
                        "충전 완료", UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()),
                new ChargingSessionDto(UUID.randomUUID(), OffsetDateTime.now().minusMinutes(30), null,
                        "충전 중", UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
        );
    }

    public List<ChargingSessionDto> findAll() {
        return mockData();
    }

    public ChargingSessionDto findById(UUID sessionId) {
        return new ChargingSessionDto(sessionId, OffsetDateTime.now().minusHours(2), OffsetDateTime.now().minusHours(1),
                "충전 완료", UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    }

    public ChargingSessionDto create(ChargingSessionDto request) {
        return new ChargingSessionDto(UUID.randomUUID(), request.startTime(), request.endTime(),
                request.changeState(), request.carId(), request.chargeId(), request.thermalId());
    }

    public ChargingSessionDto update(UUID sessionId, ChargingSessionDto request) {
        return new ChargingSessionDto(sessionId, request.startTime(), request.endTime(),
                request.changeState(), request.carId(), request.chargeId(), request.thermalId());
    }

    public void delete(UUID sessionId) {
        // TODO: ERD 확정 후 실제 삭제 로직 연결
    }
}
