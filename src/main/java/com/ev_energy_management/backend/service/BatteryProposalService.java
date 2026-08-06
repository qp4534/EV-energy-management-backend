package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.BatteryProposalDto;
import com.ev_energy_management.backend.entity.BatteryProposalEntity;
import com.ev_energy_management.backend.repository.BatteryProposalRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BatteryProposalService {

    private final BatteryProposalRepository batteryProposalRepository;

    public BatteryProposalService(BatteryProposalRepository batteryProposalRepository) {
        this.batteryProposalRepository = batteryProposalRepository;
    }

    public List<BatteryProposalDto> findAll() {
        return batteryProposalRepository.findAll().stream().map(this::toDto).toList();
    }

    public BatteryProposalDto findById(UUID proposalId) {
        return toDto(batteryProposalRepository.findById(proposalId)
                .orElseThrow(() -> new EntityNotFoundException("Battery proposal not found: " + proposalId)));
    }

    public BatteryProposalDto create(BatteryProposalDto request) {
        BatteryProposalEntity entity = BatteryProposalEntity.builder()
                .totalPrice(request.totalPrice())
                .pricePerKwh(request.pricePerKwh())
                .capacityRange(request.capacityRange())
                .suitabilityReason(request.suitabilityReason())
                .noticeText(request.noticeText())
                .batteryId(request.batteryId())
                .build();
        return toDto(batteryProposalRepository.save(entity));
    }

    public BatteryProposalDto update(UUID proposalId, BatteryProposalDto request) {
        BatteryProposalEntity entity = batteryProposalRepository.findById(proposalId)
                .orElseThrow(() -> new EntityNotFoundException("Battery proposal not found: " + proposalId));
        entity.setTotalPrice(request.totalPrice());
        entity.setPricePerKwh(request.pricePerKwh());
        entity.setCapacityRange(request.capacityRange());
        entity.setSuitabilityReason(request.suitabilityReason());
        entity.setNoticeText(request.noticeText());
        entity.setBatteryId(request.batteryId());
        return toDto(batteryProposalRepository.save(entity));
    }

    public void delete(UUID proposalId) {
        batteryProposalRepository.deleteById(proposalId);
    }

    private BatteryProposalDto toDto(BatteryProposalEntity entity) {
        return new BatteryProposalDto(
                entity.getProposalId(),
                entity.getTotalPrice(),
                entity.getPricePerKwh(),
                entity.getCapacityRange(),
                entity.getSuitabilityReason(),
                entity.getNoticeText(),
                entity.getCreatedAt(),
                entity.getBatteryId()
        );
    }
}
