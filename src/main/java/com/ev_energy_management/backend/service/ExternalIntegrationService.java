package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.ExternalIntegrationDto;
import com.ev_energy_management.backend.entity.ExternalIntegrationEntity;
import com.ev_energy_management.backend.repository.ExternalIntegrationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ExternalIntegrationService {

    private final ExternalIntegrationRepository externalIntegrationRepository;

    public ExternalIntegrationService(ExternalIntegrationRepository externalIntegrationRepository) {
        this.externalIntegrationRepository = externalIntegrationRepository;
    }

    public List<ExternalIntegrationDto> findAll() {
        return externalIntegrationRepository.findAll().stream().map(this::toDto).toList();
    }

    public ExternalIntegrationDto findById(UUID integrationId) {
        return toDto(externalIntegrationRepository.findById(integrationId)
                .orElseThrow(() -> new EntityNotFoundException("External integration not found: " + integrationId)));
    }

    public ExternalIntegrationDto create(ExternalIntegrationDto request) {
        ExternalIntegrationEntity entity = ExternalIntegrationEntity.builder()
                .name(request.name())
                .description(request.description())
                .apiKey(request.apiKey())
                .isStatus(request.isStatus() != null ? request.isStatus() : true)
                .lastConnectedAt(request.lastConnectedAt())
                .build();
        return toDto(externalIntegrationRepository.save(entity));
    }

    public ExternalIntegrationDto update(UUID integrationId, ExternalIntegrationDto request) {
        ExternalIntegrationEntity entity = externalIntegrationRepository.findById(integrationId)
                .orElseThrow(() -> new EntityNotFoundException("External integration not found: " + integrationId));
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setApiKey(request.apiKey());
        entity.setIsStatus(request.isStatus());
        entity.setLastConnectedAt(request.lastConnectedAt());
        return toDto(externalIntegrationRepository.save(entity));
    }

    public void delete(UUID integrationId) {
        externalIntegrationRepository.deleteById(integrationId);
    }

    private ExternalIntegrationDto toDto(ExternalIntegrationEntity entity) {
        return new ExternalIntegrationDto(
                entity.getIntegrationId(),
                entity.getName(),
                entity.getDescription(),
                entity.getApiKey(),
                entity.getIsStatus(),
                entity.getLastConnectedAt(),
                entity.getCreatedAt()
        );
    }
}
