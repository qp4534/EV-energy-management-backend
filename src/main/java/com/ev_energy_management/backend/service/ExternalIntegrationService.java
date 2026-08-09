package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.ExternalIntegrationDto;
import com.ev_energy_management.backend.entity.ExternalIntegrationEntity;
import com.ev_energy_management.backend.repository.ExternalIntegrationRepository;
import com.ev_energy_management.backend.util.MaskingUtils;
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

    public ExternalIntegrationDto reissueKey(UUID integrationId) {
        ExternalIntegrationEntity entity = externalIntegrationRepository.findById(integrationId)
                .orElseThrow(() -> new EntityNotFoundException("External integration not found: " + integrationId));

        String newKey = "sk_live_" + UUID.randomUUID().toString().replace("-", "");
        entity.setApiKey(newKey);
        entity.setLastConnectedAt(null); // 재발급했으니 재연결 전까지는 미연결 상태로
        ExternalIntegrationEntity saved = externalIntegrationRepository.save(entity);

        return new ExternalIntegrationDto(
                saved.getIntegrationId(),
                saved.getName(),
                saved.getDescription(),
                saved.getApiKey(), // 마스킹 없이 원본 그대로 (재발급 직후 1회만)
                saved.getIsStatus(),
                saved.getLastConnectedAt(),
                saved.getCreatedAt()
        );
    }

    private ExternalIntegrationDto toDto(ExternalIntegrationEntity entity) {
        return new ExternalIntegrationDto(
                entity.getIntegrationId(),
                entity.getName(),
                entity.getDescription(),
                MaskingUtils.maskApiKey(entity.getApiKey()), // 항상 마스킹 (재발급 응답만 예외)
                entity.getIsStatus(),
                entity.getLastConnectedAt(),
                entity.getCreatedAt()
        );
    }
}
