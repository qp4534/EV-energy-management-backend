package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.BatteryDiagnosisMetricDto;
import com.ev_energy_management.backend.entity.BatteryDiagnosisMetricEntity;
import com.ev_energy_management.backend.repository.BatteryDiagnosisMetricRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BatteryDiagnosisMetricService {

    private final BatteryDiagnosisMetricRepository batteryDiagnosisMetricRepository;

    public BatteryDiagnosisMetricService(BatteryDiagnosisMetricRepository batteryDiagnosisMetricRepository) {
        this.batteryDiagnosisMetricRepository = batteryDiagnosisMetricRepository;
    }

    public List<BatteryDiagnosisMetricDto> findAll() {
        return batteryDiagnosisMetricRepository.findAll().stream().map(this::toDto).toList();
    }

    public BatteryDiagnosisMetricDto findById(UUID metricId) {
        return toDto(batteryDiagnosisMetricRepository.findById(metricId)
                .orElseThrow(() -> new EntityNotFoundException("Battery diagnosis metric not found: " + metricId)));
    }

    public BatteryDiagnosisMetricDto create(BatteryDiagnosisMetricDto request) {
        BatteryDiagnosisMetricEntity entity = BatteryDiagnosisMetricEntity.builder()
                .remainingLifeScore(request.remainingLifeScore())
                .dischargePowerScore(request.dischargePowerScore())
                .chargeHealthScore(request.chargeHealthScore())
                .voltageStabilityScore(request.voltageStabilityScore())
                .batteryId(request.batteryId())
                .build();
        return toDto(batteryDiagnosisMetricRepository.save(entity));
    }

    public BatteryDiagnosisMetricDto update(UUID metricId, BatteryDiagnosisMetricDto request) {
        BatteryDiagnosisMetricEntity entity = batteryDiagnosisMetricRepository.findById(metricId)
                .orElseThrow(() -> new EntityNotFoundException("Battery diagnosis metric not found: " + metricId));
        entity.setRemainingLifeScore(request.remainingLifeScore());
        entity.setDischargePowerScore(request.dischargePowerScore());
        entity.setChargeHealthScore(request.chargeHealthScore());
        entity.setVoltageStabilityScore(request.voltageStabilityScore());
        entity.setBatteryId(request.batteryId());
        return toDto(batteryDiagnosisMetricRepository.save(entity));
    }

    public void delete(UUID metricId) {
        batteryDiagnosisMetricRepository.deleteById(metricId);
    }

    private BatteryDiagnosisMetricDto toDto(BatteryDiagnosisMetricEntity entity) {
        return new BatteryDiagnosisMetricDto(
                entity.getMetricId(),
                entity.getRemainingLifeScore(),
                entity.getDischargePowerScore(),
                entity.getChargeHealthScore(),
                entity.getVoltageStabilityScore(),
                entity.getDiagnosedAt(),
                entity.getBatteryId()
        );
    }
}
