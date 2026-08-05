package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.ChargerDto;
import com.ev_energy_management.backend.service.ChargerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chargers")
public class ChargerController {

    private final ChargerService chargerService;

    public ChargerController(ChargerService chargerService) {
        this.chargerService = chargerService;
    }

    @GetMapping
    public List<ChargerDto> getChargers() {
        return chargerService.findAll();
    }

    @GetMapping("/{chargerId}")
    public ChargerDto getCharger(@PathVariable UUID chargerId) {
        return chargerService.findById(chargerId);
    }

    @PostMapping
    public ResponseEntity<ChargerDto> createCharger(@RequestBody ChargerDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chargerService.create(request));
    }

    @PutMapping("/{chargerId}")
    public ChargerDto updateCharger(@PathVariable UUID chargerId, @RequestBody ChargerDto request) {
        return chargerService.update(chargerId, request);
    }

    @DeleteMapping("/{chargerId}")
    public ResponseEntity<Void> deleteCharger(@PathVariable UUID chargerId) {
        chargerService.delete(chargerId);
        return ResponseEntity.noContent().build();
    }
}
