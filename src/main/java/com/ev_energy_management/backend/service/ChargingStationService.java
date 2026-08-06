package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.ChargingStationDto;
import com.ev_energy_management.backend.entity.ChargingStationEntity;
import com.ev_energy_management.backend.repository.ChargingStationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ChargingStationService {

    private final ChargingStationRepository chargingStationRepository;

    public ChargingStationService(ChargingStationRepository chargingStationRepository) {
        this.chargingStationRepository = chargingStationRepository;
    }

    public List<ChargingStationDto> findAll() {
        return chargingStationRepository.findAll().stream().map(this::toDto).toList();
    }

    public ChargingStationDto findById(UUID chargeId) {
        return toDto(chargingStationRepository.findById(chargeId)
                .orElseThrow(() -> new EntityNotFoundException("Charging station not found: " + chargeId)));
    }

    public ChargingStationDto create(ChargingStationDto request) {
        ChargingStationEntity entity = ChargingStationEntity.builder()
                .region(request.region())
                .address(request.address())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .name(request.name())
                .slowChargerCount(request.slowChargerCount() != null ? request.slowChargerCount() : 0)
                .fastChargerCount(request.fastChargerCount() != null ? request.fastChargerCount() : 0)
                .availableCount(request.availableCount() != null ? request.availableCount() : 0)
                .minQueueLength(request.minQueueLength())
                .minWaitingTime(request.minWaitingTime())
                .build();
        return toDto(chargingStationRepository.save(entity));
    }

    public ChargingStationDto update(UUID chargeId, ChargingStationDto request) {
        ChargingStationEntity entity = chargingStationRepository.findById(chargeId)
                .orElseThrow(() -> new EntityNotFoundException("Charging station not found: " + chargeId));
        entity.setRegion(request.region());
        entity.setAddress(request.address());
        entity.setLatitude(request.latitude());
        entity.setLongitude(request.longitude());
        entity.setName(request.name());
        entity.setSlowChargerCount(request.slowChargerCount());
        entity.setFastChargerCount(request.fastChargerCount());
        entity.setAvailableCount(request.availableCount());
        entity.setMinQueueLength(request.minQueueLength());
        entity.setMinWaitingTime(request.minWaitingTime());
        return toDto(chargingStationRepository.save(entity));
    }

    public void delete(UUID chargeId) {
        chargingStationRepository.deleteById(chargeId);
    }

    private ChargingStationDto toDto(ChargingStationEntity entity) {
        return new ChargingStationDto(
                entity.getChargeId(),
                entity.getRegion(),
                entity.getAddress(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getName(),
                entity.getSlowChargerCount(),
                entity.getFastChargerCount(),
                entity.getAvailableCount(),
                entity.getMinQueueLength(),
                entity.getMinWaitingTime()
        );
    }
}
