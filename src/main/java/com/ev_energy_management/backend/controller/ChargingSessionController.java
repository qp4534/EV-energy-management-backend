package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.ChargingSessionDto;
import com.ev_energy_management.backend.service.ChargingSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/charging-sessions")
public class ChargingSessionController {

    private final ChargingSessionService chargingSessionService;

    public ChargingSessionController(ChargingSessionService chargingSessionService) {
        this.chargingSessionService = chargingSessionService;
    }

    @GetMapping
    public List<ChargingSessionDto> getChargingSessions() {
        return chargingSessionService.findAll();
    }

    @GetMapping("/{sessionId}")
    public ChargingSessionDto getChargingSession(@PathVariable UUID sessionId) {
        return chargingSessionService.findById(sessionId);
    }

    @PostMapping
    public ResponseEntity<ChargingSessionDto> createChargingSession(@RequestBody ChargingSessionDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chargingSessionService.create(request));
    }

    @PutMapping("/{sessionId}")
    public ChargingSessionDto updateChargingSession(@PathVariable UUID sessionId, @RequestBody ChargingSessionDto request) {
        return chargingSessionService.update(sessionId, request);
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> deleteChargingSession(@PathVariable UUID sessionId) {
        chargingSessionService.delete(sessionId);
        return ResponseEntity.noContent().build();
    }
}
