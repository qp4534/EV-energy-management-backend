package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.BatteryPassportDto;
import com.ev_energy_management.backend.entity.BatteryPassportEntity;
import com.ev_energy_management.backend.repository.BatteryPassportRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BatteryPassportService {

    private final BatteryPassportRepository batteryPassportRepository;

    public BatteryPassportService(BatteryPassportRepository batteryPassportRepository) {
        this.batteryPassportRepository = batteryPassportRepository;
    }

    public List<BatteryPassportDto> findAll() {
        return batteryPassportRepository.findAll().stream().map(this::toDto).toList();
    }

    public BatteryPassportDto findById(UUID batteryId) {
        return toDto(batteryPassportRepository.findById(batteryId)
                .orElseThrow(() -> new EntityNotFoundException("Battery passport not found: " + batteryId)));
    }

    public BatteryPassportDto create(BatteryPassportDto request) {
        BatteryPassportEntity entity = BatteryPassportEntity.builder()
                .manufacturer(request.manufacturer())
                .batteryType(request.batteryType())
                .ratedCapacity(request.ratedCapacity())
                .sohScore(request.sohScore())
                .chargeCycles(request.chargeCycles())
                .currentTemp(request.currentTemp())
                .lastInspectedAt(request.lastInspectedAt())
                .carId(request.carId())
                .batteryLevel(request.batteryLevel() != null ? request.batteryLevel() : "미등록")
                .remainingCycles(request.remainingCycles())
                .totalCycles(request.totalCycles())
                .reuseStatus(request.reuseStatus())
                .gradeDetail(request.gradeDetail())
                .reliabilityScore(request.reliabilityScore())
                .reuseProbabilities(request.reuseProbabilities())
                .voltage(request.voltage())
                .current(request.current())
                .rul(request.rul())
                .manufacturedAt(request.manufacturedAt())
                .installedAt(request.installedAt())
                .build();
        return toDto(batteryPassportRepository.save(entity));
    }

    public BatteryPassportDto update(UUID batteryId, BatteryPassportDto request) {
        BatteryPassportEntity entity = batteryPassportRepository.findById(batteryId)
                .orElseThrow(() -> new EntityNotFoundException("Battery passport not found: " + batteryId));
        entity.setManufacturer(request.manufacturer());
        entity.setBatteryType(request.batteryType());
        entity.setRatedCapacity(request.ratedCapacity());
        entity.setSohScore(request.sohScore());
        entity.setChargeCycles(request.chargeCycles());
        entity.setCurrentTemp(request.currentTemp());
        entity.setLastInspectedAt(request.lastInspectedAt());
        entity.setCarId(request.carId());
        entity.setBatteryLevel(request.batteryLevel());
        entity.setRemainingCycles(request.remainingCycles());
        entity.setTotalCycles(request.totalCycles());
        entity.setReuseStatus(request.reuseStatus());
        entity.setGradeDetail(request.gradeDetail());
        entity.setReliabilityScore(request.reliabilityScore());
        entity.setReuseProbabilities(request.reuseProbabilities());
        entity.setVoltage(request.voltage());
        entity.setCurrent(request.current());
        entity.setRul(request.rul());
        entity.setManufacturedAt(request.manufacturedAt());
        entity.setInstalledAt(request.installedAt());
        return toDto(batteryPassportRepository.save(entity));
    }

    public void delete(UUID batteryId) {
        batteryPassportRepository.deleteById(batteryId);
    }

    private BatteryPassportDto toDto(BatteryPassportEntity entity) {
        return new BatteryPassportDto(
                entity.getBatteryId(),
                entity.getManufacturer(),
                entity.getBatteryType(),
                entity.getRatedCapacity(),
                entity.getSohScore(),
                entity.getChargeCycles(),
                entity.getCurrentTemp(),
                entity.getLastInspectedAt(),
                entity.getCarId(),
                entity.getBatteryLevel(),
                entity.getRemainingCycles(),
                entity.getTotalCycles(),
                entity.getReuseStatus(),
                entity.getGradeDetail(),
                entity.getReliabilityScore(),
                entity.getReuseProbabilities(),
                entity.getVoltage(),
                entity.getCurrent(),
                entity.getRul(),
                entity.getManufacturedAt(),
                entity.getInstalledAt()
        );
    }
}
