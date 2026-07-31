package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.BatteryPassportDto;
import com.ev_energy_management.backend.service.BatteryPassportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/battery-passports")
public class BatteryPassportController {

    private final BatteryPassportService batteryPassportService;

    public BatteryPassportController(BatteryPassportService batteryPassportService) {
        this.batteryPassportService = batteryPassportService;
    }

    @GetMapping
    public List<BatteryPassportDto> getBatteryPassports() {
        return batteryPassportService.findAll();
    }

    @GetMapping("/{batteryId}")
    public BatteryPassportDto getBatteryPassport(@PathVariable UUID batteryId) {
        return batteryPassportService.findById(batteryId);
    }

    @PostMapping
    public ResponseEntity<BatteryPassportDto> createBatteryPassport(@RequestBody BatteryPassportDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(batteryPassportService.create(request));
    }

    @PutMapping("/{batteryId}")
    public BatteryPassportDto updateBatteryPassport(@PathVariable UUID batteryId, @RequestBody BatteryPassportDto request) {
        return batteryPassportService.update(batteryId, request);
    }

    @DeleteMapping("/{batteryId}")
    public ResponseEntity<Void> deleteBatteryPassport(@PathVariable UUID batteryId) {
        batteryPassportService.delete(batteryId);
        return ResponseEntity.noContent().build();
    }
}
