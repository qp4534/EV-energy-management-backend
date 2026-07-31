package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.ThermalVideoStreamDto;
import com.ev_energy_management.backend.service.ThermalVideoStreamService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/thermal-video-streams")
public class ThermalVideoStreamController {

    private final ThermalVideoStreamService thermalVideoStreamService;

    public ThermalVideoStreamController(ThermalVideoStreamService thermalVideoStreamService) {
        this.thermalVideoStreamService = thermalVideoStreamService;
    }

    @GetMapping
    public List<ThermalVideoStreamDto> getThermalVideoStreams() {
        return thermalVideoStreamService.findAll();
    }

    @GetMapping("/{thermalId}")
    public ThermalVideoStreamDto getThermalVideoStream(@PathVariable UUID thermalId) {
        return thermalVideoStreamService.findById(thermalId);
    }

    @PostMapping
    public ResponseEntity<ThermalVideoStreamDto> createThermalVideoStream(@RequestBody ThermalVideoStreamDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(thermalVideoStreamService.create(request));
    }

    @PutMapping("/{thermalId}")
    public ThermalVideoStreamDto updateThermalVideoStream(@PathVariable UUID thermalId, @RequestBody ThermalVideoStreamDto request) {
        return thermalVideoStreamService.update(thermalId, request);
    }

    @DeleteMapping("/{thermalId}")
    public ResponseEntity<Void> deleteThermalVideoStream(@PathVariable UUID thermalId) {
        thermalVideoStreamService.delete(thermalId);
        return ResponseEntity.noContent().build();
    }
}
