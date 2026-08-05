package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.ChargingSessionDto;
import com.ev_energy_management.backend.entity.ChargingSessionEntity;
import com.ev_energy_management.backend.repository.ChargingSessionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ChargingSessionService {

    private final ChargingSessionRepository chargingSessionRepository;

    public ChargingSessionService(ChargingSessionRepository chargingSessionRepository) {
        this.chargingSessionRepository = chargingSessionRepository;
    }

    public List<ChargingSessionDto> findAll() {
        return chargingSessionRepository.findAll().stream().map(this::toDto).toList();
    }

    public ChargingSessionDto findById(UUID sessionId) {
        return toDto(chargingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Charging session not found: " + sessionId)));
    }

    public ChargingSessionDto create(ChargingSessionDto request) {
        ChargingSessionEntity entity = ChargingSessionEntity.builder()
                .startTime(request.startTime())
                .endTime(request.endTime())
                .changeState(request.changeState() != null ? request.changeState() : "대기중")
                .carId(request.carId())
                .chargerId(request.chargerId())
                .startSoc(request.startSoc())
                .endSoc(request.endSoc())
                .build();
        return toDto(chargingSessionRepository.save(entity));
    }

    public ChargingSessionDto update(UUID sessionId, ChargingSessionDto request) {
        ChargingSessionEntity entity = chargingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Charging session not found: " + sessionId));
        entity.setStartTime(request.startTime());
        entity.setEndTime(request.endTime());
        entity.setChangeState(request.changeState());
        entity.setCarId(request.carId());
        entity.setChargerId(request.chargerId());
        entity.setStartSoc(request.startSoc());
        entity.setEndSoc(request.endSoc());
        return toDto(chargingSessionRepository.save(entity));
    }

    public void delete(UUID sessionId) {
        chargingSessionRepository.deleteById(sessionId);
    }

    private ChargingSessionDto toDto(ChargingSessionEntity entity) {
        return new ChargingSessionDto(
                entity.getSessionId(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getChangeState(),
                entity.getCarId(),
                entity.getChargerId(),
                entity.getStartSoc(),
                entity.getEndSoc()
        );
    }
}
