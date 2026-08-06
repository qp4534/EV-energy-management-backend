package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.ChargerDto;
import com.ev_energy_management.backend.entity.ChargerEntity;
import com.ev_energy_management.backend.repository.ChargerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ChargerService {

    private final ChargerRepository chargerRepository;

    public ChargerService(ChargerRepository chargerRepository) {
        this.chargerRepository = chargerRepository;
    }

    public List<ChargerDto> findAll() {
        return chargerRepository.findAll().stream().map(this::toDto).toList();
    }

    public ChargerDto findById(UUID chargerId) {
        return toDto(chargerRepository.findById(chargerId)
                .orElseThrow(() -> new EntityNotFoundException("Charger not found: " + chargerId)));
    }

    public ChargerDto create(ChargerDto request) {
        ChargerEntity entity = ChargerEntity.builder()
                .chargerType(request.chargerType())
                .ratedPowerKw(request.ratedPowerKw())
                .status(request.status() != null ? request.status() : "사용가능")
                .queueLength(request.queueLength() != null ? request.queueLength() : 0)
                .waitingTimeMin(request.waitingTimeMin())
                .chargeId(request.chargeId())
                .build();
        return toDto(chargerRepository.save(entity));
    }

    public ChargerDto update(UUID chargerId, ChargerDto request) {
        ChargerEntity entity = chargerRepository.findById(chargerId)
                .orElseThrow(() -> new EntityNotFoundException("Charger not found: " + chargerId));
        entity.setChargerType(request.chargerType());
        entity.setRatedPowerKw(request.ratedPowerKw());
        entity.setStatus(request.status());
        entity.setQueueLength(request.queueLength());
        entity.setWaitingTimeMin(request.waitingTimeMin());
        entity.setChargeId(request.chargeId());
        return toDto(chargerRepository.save(entity));
    }

    public void delete(UUID chargerId) {
        chargerRepository.deleteById(chargerId);
    }

    private ChargerDto toDto(ChargerEntity entity) {
        return new ChargerDto(
                entity.getChargerId(),
                entity.getChargerType(),
                entity.getRatedPowerKw(),
                entity.getStatus(),
                entity.getQueueLength(),
                entity.getWaitingTimeMin(),
                entity.getUpdatedAt(),
                entity.getChargeId()
        );
    }
}
