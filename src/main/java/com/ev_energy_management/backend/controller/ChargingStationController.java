package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.ChargingStationDto;
import com.ev_energy_management.backend.service.ChargingStationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/charging-stations")
public class ChargingStationController {

    private final ChargingStationService chargingStationService;

    public ChargingStationController(ChargingStationService chargingStationService) {
        this.chargingStationService = chargingStationService;
    }

    @GetMapping
    public List<ChargingStationDto> getChargingStations() {
        return chargingStationService.findAll();
    }

    @GetMapping("/{chargeId}")
    public ChargingStationDto getChargingStation(@PathVariable UUID chargeId) {
        return chargingStationService.findById(chargeId);
    }

    @PostMapping
    public ResponseEntity<ChargingStationDto> createChargingStation(@RequestBody ChargingStationDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chargingStationService.create(request));
    }

    @PutMapping("/{chargeId}")
    public ChargingStationDto updateChargingStation(@PathVariable UUID chargeId, @RequestBody ChargingStationDto request) {
        return chargingStationService.update(chargeId, request);
    }

    @DeleteMapping("/{chargeId}")
    public ResponseEntity<Void> deleteChargingStation(@PathVariable UUID chargeId) {
        chargingStationService.delete(chargeId);
        return ResponseEntity.noContent().build();
    }
}
