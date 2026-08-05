package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.TwinFrameDto;
import com.ev_energy_management.backend.service.TwinFrameService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/twin-frames")
public class TwinFrameController {

    private final TwinFrameService twinFrameService;

    public TwinFrameController(TwinFrameService twinFrameService) {
        this.twinFrameService = twinFrameService;
    }

    @GetMapping
    public List<TwinFrameDto> getTwinFrames() {
        return twinFrameService.findAll();
    }

    @GetMapping("/{frameId}")
    public TwinFrameDto getTwinFrame(@PathVariable UUID frameId) {
        return twinFrameService.findById(frameId);
    }

    @GetMapping("/car/{carId}")
    public List<TwinFrameDto> getTwinFramesByCar(@PathVariable UUID carId) {
        return twinFrameService.findByCarId(carId);
    }

    @PostMapping
    public ResponseEntity<TwinFrameDto> createTwinFrame(@RequestBody TwinFrameDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(twinFrameService.create(request));
    }

    @PutMapping("/{frameId}")
    public TwinFrameDto updateTwinFrame(@PathVariable UUID frameId, @RequestBody TwinFrameDto request) {
        return twinFrameService.update(frameId, request);
    }

    @DeleteMapping("/{frameId}")
    public ResponseEntity<Void> deleteTwinFrame(@PathVariable UUID frameId) {
        twinFrameService.delete(frameId);
        return ResponseEntity.noContent().build();
    }
}
