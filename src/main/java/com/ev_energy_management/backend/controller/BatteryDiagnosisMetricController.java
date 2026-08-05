package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.BatteryDiagnosisMetricDto;
import com.ev_energy_management.backend.service.BatteryDiagnosisMetricService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/battery-diagnosis-metrics")
public class BatteryDiagnosisMetricController {

    private final BatteryDiagnosisMetricService batteryDiagnosisMetricService;

    public BatteryDiagnosisMetricController(BatteryDiagnosisMetricService batteryDiagnosisMetricService) {
        this.batteryDiagnosisMetricService = batteryDiagnosisMetricService;
    }

    @GetMapping
    public List<BatteryDiagnosisMetricDto> getBatteryDiagnosisMetrics() {
        return batteryDiagnosisMetricService.findAll();
    }

    @GetMapping("/{metricId}")
    public BatteryDiagnosisMetricDto getBatteryDiagnosisMetric(@PathVariable UUID metricId) {
        return batteryDiagnosisMetricService.findById(metricId);
    }

    @PostMapping
    public ResponseEntity<BatteryDiagnosisMetricDto> createBatteryDiagnosisMetric(@RequestBody BatteryDiagnosisMetricDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(batteryDiagnosisMetricService.create(request));
    }

    @PutMapping("/{metricId}")
    public BatteryDiagnosisMetricDto updateBatteryDiagnosisMetric(@PathVariable UUID metricId, @RequestBody BatteryDiagnosisMetricDto request) {
        return batteryDiagnosisMetricService.update(metricId, request);
    }

    @DeleteMapping("/{metricId}")
    public ResponseEntity<Void> deleteBatteryDiagnosisMetric(@PathVariable UUID metricId) {
        batteryDiagnosisMetricService.delete(metricId);
        return ResponseEntity.noContent().build();
    }
}
