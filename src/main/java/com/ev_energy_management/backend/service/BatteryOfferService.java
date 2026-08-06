package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.BatteryOfferDto;
import com.ev_energy_management.backend.entity.BatteryOfferEntity;
import com.ev_energy_management.backend.repository.BatteryOfferRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BatteryOfferService {

    private final BatteryOfferRepository batteryOfferRepository;

    public BatteryOfferService(BatteryOfferRepository batteryOfferRepository) {
        this.batteryOfferRepository = batteryOfferRepository;
    }

    public List<BatteryOfferDto> findAll() {
        return batteryOfferRepository.findAll().stream().map(this::toDto).toList();
    }

    public BatteryOfferDto findById(UUID offerId) {
        return toDto(batteryOfferRepository.findById(offerId)
                .orElseThrow(() -> new EntityNotFoundException("Battery offer not found: " + offerId)));
    }

    public BatteryOfferDto create(BatteryOfferDto request) {
        BatteryOfferEntity entity = BatteryOfferEntity.builder()
                .buyerName(request.buyerName())
                .businessType(request.businessType())
                .offeredPrice(request.offeredPrice())
                .pricePerKwh(request.pricePerKwh())
                .rankOrder(request.rankOrder())
                .description(request.description())
                .batteryId(request.batteryId())
                .build();
        return toDto(batteryOfferRepository.save(entity));
    }

    public BatteryOfferDto update(UUID offerId, BatteryOfferDto request) {
        BatteryOfferEntity entity = batteryOfferRepository.findById(offerId)
                .orElseThrow(() -> new EntityNotFoundException("Battery offer not found: " + offerId));
        entity.setBuyerName(request.buyerName());
        entity.setBusinessType(request.businessType());
        entity.setOfferedPrice(request.offeredPrice());
        entity.setPricePerKwh(request.pricePerKwh());
        entity.setRankOrder(request.rankOrder());
        entity.setDescription(request.description());
        entity.setBatteryId(request.batteryId());
        return toDto(batteryOfferRepository.save(entity));
    }

    public void delete(UUID offerId) {
        batteryOfferRepository.deleteById(offerId);
    }

    private BatteryOfferDto toDto(BatteryOfferEntity entity) {
        return new BatteryOfferDto(
                entity.getOfferId(),
                entity.getBuyerName(),
                entity.getBusinessType(),
                entity.getOfferedPrice(),
                entity.getPricePerKwh(),
                entity.getRankOrder(),
                entity.getDescription(),
                entity.getBatteryId()
        );
    }
}
