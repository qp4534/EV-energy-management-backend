package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.AnomalyLogDto;
import com.ev_energy_management.backend.entity.AnomalyLogEntity;
import com.ev_energy_management.backend.repository.AnomalyLogRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AnomalyLogService {

    private final AnomalyLogRepository anomalyLogRepository;

    public AnomalyLogService(AnomalyLogRepository anomalyLogRepository) {
        this.anomalyLogRepository = anomalyLogRepository;
    }

    public List<AnomalyLogDto> findAll() {
        return anomalyLogRepository.findAll().stream().map(this::toDto).toList();
    }

    public AnomalyLogDto findById(UUID anomalyId) {
        return toDto(anomalyLogRepository.findById(anomalyId)
                .orElseThrow(() -> new EntityNotFoundException("Anomaly log not found: " + anomalyId)));
    }

    public AnomalyLogDto create(AnomalyLogDto request) {
        AnomalyLogEntity entity = AnomalyLogEntity.builder()
                .abnormalType(request.abnormalType())
                .sourceType(request.sourceType())
                .triggerValue(request.triggerValue())
                .riskLevel(request.riskLevel() != null ? request.riskLevel() : "정상")
                .carId(request.carId())
                .sessionId(request.sessionId())
                .build();
        return toDto(anomalyLogRepository.save(entity));
    }

    public AnomalyLogDto update(UUID anomalyId, AnomalyLogDto request) {
        AnomalyLogEntity entity = anomalyLogRepository.findById(anomalyId)
                .orElseThrow(() -> new EntityNotFoundException("Anomaly log not found: " + anomalyId));
        entity.setAbnormalType(request.abnormalType());
        entity.setSourceType(request.sourceType());
        entity.setTriggerValue(request.triggerValue());
        entity.setRiskLevel(request.riskLevel());
        entity.setCarId(request.carId());
        entity.setSessionId(request.sessionId());
        return toDto(anomalyLogRepository.save(entity));
    }

    public void delete(UUID anomalyId) {
        anomalyLogRepository.deleteById(anomalyId);
    }

    private AnomalyLogDto toDto(AnomalyLogEntity entity) {
        return new AnomalyLogDto(
                entity.getAnomalyId(),
                entity.getAbnormalType(),
                entity.getSourceType(),
                entity.getTriggerValue(),
                entity.getDetectedAt(),
                entity.getRiskLevel(),
                entity.getCarId(),
                entity.getSessionId()
        );
    }
}
