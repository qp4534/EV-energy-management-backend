package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.ExternalIntegrationDto;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ExternalIntegrationService {

    private List<ExternalIntegrationDto> mockData() {
        return List.of(
                new ExternalIntegrationDto(UUID.randomUUID(), "IoT 디바이스 게이트웨이", "배터리 센서 실시간 수집",
                        "masked-api-key-1", true, OffsetDateTime.now(), OffsetDateTime.now().minusMonths(2)),
                new ExternalIntegrationDto(UUID.randomUUID(), "결제 게이트웨이", "배터리 매입 결제 연동",
                        "masked-api-key-2", false, null, OffsetDateTime.now().minusMonths(1))
        );
    }

    public List<ExternalIntegrationDto> findAll() {
        return mockData();
    }

    public ExternalIntegrationDto findById(UUID integrationId) {
        return new ExternalIntegrationDto(integrationId, "IoT 디바이스 게이트웨이", "배터리 센서 실시간 수집",
                "masked-api-key-1", true, OffsetDateTime.now(), OffsetDateTime.now().minusMonths(2));
    }

    public ExternalIntegrationDto create(ExternalIntegrationDto request) {
        return new ExternalIntegrationDto(UUID.randomUUID(), request.name(), request.description(), request.apiKey(),
                request.isStatus(), request.lastConnectedAt(), OffsetDateTime.now());
    }

    public ExternalIntegrationDto update(UUID integrationId, ExternalIntegrationDto request) {
        return new ExternalIntegrationDto(integrationId, request.name(), request.description(), request.apiKey(),
                request.isStatus(), request.lastConnectedAt(), request.createdAt());
    }

    public void delete(UUID integrationId) {
        // TODO: ERD 확정 후 실제 삭제 로직 연결
    }
}
