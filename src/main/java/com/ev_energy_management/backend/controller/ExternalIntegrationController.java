package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.ExternalIntegrationDto;
import com.ev_energy_management.backend.service.ExternalIntegrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/external-integrations")
public class ExternalIntegrationController {

    private final ExternalIntegrationService externalIntegrationService;

    public ExternalIntegrationController(ExternalIntegrationService externalIntegrationService) {
        this.externalIntegrationService = externalIntegrationService;
    }

    @GetMapping
    public List<ExternalIntegrationDto> getExternalIntegrations() {
        return externalIntegrationService.findAll();
    }

    @GetMapping("/{integrationId}")
    public ExternalIntegrationDto getExternalIntegration(@PathVariable UUID integrationId) {
        return externalIntegrationService.findById(integrationId);
    }

    @PostMapping
    public ResponseEntity<ExternalIntegrationDto> createExternalIntegration(@RequestBody ExternalIntegrationDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(externalIntegrationService.create(request));
    }

    @PutMapping("/{integrationId}")
    public ExternalIntegrationDto updateExternalIntegration(@PathVariable UUID integrationId, @RequestBody ExternalIntegrationDto request) {
        return externalIntegrationService.update(integrationId, request);
    }

    @DeleteMapping("/{integrationId}")
    public ResponseEntity<Void> deleteExternalIntegration(@PathVariable UUID integrationId) {
        externalIntegrationService.delete(integrationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{integrationId}/reissue")
    public ExternalIntegrationDto reissueKey(@PathVariable UUID integrationId) {
        return externalIntegrationService.reissueKey(integrationId);
    }
}
