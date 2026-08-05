package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.BatteryOfferDto;
import com.ev_energy_management.backend.service.BatteryOfferService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/battery-offers")
public class BatteryOfferController {

    private final BatteryOfferService batteryOfferService;

    public BatteryOfferController(BatteryOfferService batteryOfferService) {
        this.batteryOfferService = batteryOfferService;
    }

    @GetMapping
    public List<BatteryOfferDto> getBatteryOffers() {
        return batteryOfferService.findAll();
    }

    @GetMapping("/{offerId}")
    public BatteryOfferDto getBatteryOffer(@PathVariable UUID offerId) {
        return batteryOfferService.findById(offerId);
    }

    @PostMapping
    public ResponseEntity<BatteryOfferDto> createBatteryOffer(@RequestBody BatteryOfferDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(batteryOfferService.create(request));
    }

    @PutMapping("/{offerId}")
    public BatteryOfferDto updateBatteryOffer(@PathVariable UUID offerId, @RequestBody BatteryOfferDto request) {
        return batteryOfferService.update(offerId, request);
    }

    @DeleteMapping("/{offerId}")
    public ResponseEntity<Void> deleteBatteryOffer(@PathVariable UUID offerId) {
        batteryOfferService.delete(offerId);
        return ResponseEntity.noContent().build();
    }
}
