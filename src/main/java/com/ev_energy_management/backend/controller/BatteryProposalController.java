package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.BatteryProposalDto;
import com.ev_energy_management.backend.service.BatteryProposalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/battery-proposals")
public class BatteryProposalController {

    private final BatteryProposalService batteryProposalService;

    public BatteryProposalController(BatteryProposalService batteryProposalService) {
        this.batteryProposalService = batteryProposalService;
    }

    @GetMapping
    public List<BatteryProposalDto> getBatteryProposals() {
        return batteryProposalService.findAll();
    }

    @GetMapping("/{proposalId}")
    public BatteryProposalDto getBatteryProposal(@PathVariable UUID proposalId) {
        return batteryProposalService.findById(proposalId);
    }

    @PostMapping
    public ResponseEntity<BatteryProposalDto> createBatteryProposal(@RequestBody BatteryProposalDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(batteryProposalService.create(request));
    }

    @PutMapping("/{proposalId}")
    public BatteryProposalDto updateBatteryProposal(@PathVariable UUID proposalId, @RequestBody BatteryProposalDto request) {
        return batteryProposalService.update(proposalId, request);
    }

    @DeleteMapping("/{proposalId}")
    public ResponseEntity<Void> deleteBatteryProposal(@PathVariable UUID proposalId) {
        batteryProposalService.delete(proposalId);
        return ResponseEntity.noContent().build();
    }
}
