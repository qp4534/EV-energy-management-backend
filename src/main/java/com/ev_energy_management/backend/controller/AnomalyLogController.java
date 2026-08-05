package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.AnomalyLogDto;
import com.ev_energy_management.backend.service.AnomalyLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/anomaly-logs")
public class AnomalyLogController {

    private final AnomalyLogService anomalyLogService;

    public AnomalyLogController(AnomalyLogService anomalyLogService) {
        this.anomalyLogService = anomalyLogService;
    }

    @GetMapping
    public List<AnomalyLogDto> getAnomalyLogs() {
        return anomalyLogService.findAll();
    }

    @GetMapping("/{anomalyId}")
    public AnomalyLogDto getAnomalyLog(@PathVariable UUID anomalyId) {
        return anomalyLogService.findById(anomalyId);
    }

    @PostMapping
    public ResponseEntity<AnomalyLogDto> createAnomalyLog(@RequestBody AnomalyLogDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(anomalyLogService.create(request));
    }

    @PutMapping("/{anomalyId}")
    public AnomalyLogDto updateAnomalyLog(@PathVariable UUID anomalyId, @RequestBody AnomalyLogDto request) {
        return anomalyLogService.update(anomalyId, request);
    }

    @DeleteMapping("/{anomalyId}")
    public ResponseEntity<Void> deleteAnomalyLog(@PathVariable UUID anomalyId) {
        anomalyLogService.delete(anomalyId);
        return ResponseEntity.noContent().build();
    }
}
