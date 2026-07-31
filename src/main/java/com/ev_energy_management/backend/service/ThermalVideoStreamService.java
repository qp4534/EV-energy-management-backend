package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.ThermalVideoStreamDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ThermalVideoStreamService {

    private List<ThermalVideoStreamDto> mockData() {
        return List.of(
                new ThermalVideoStreamDto(UUID.randomUUID(), "https://example.com/stream/thermal-001.mp4",
                        "{\"zone\":\"battery-pack\",\"frameRate\":15}"),
                new ThermalVideoStreamDto(UUID.randomUUID(), "https://example.com/stream/thermal-002.mp4",
                        "{\"zone\":\"charging-port\",\"frameRate\":10}")
        );
    }

    public List<ThermalVideoStreamDto> findAll() {
        return mockData();
    }

    public ThermalVideoStreamDto findById(UUID thermalId) {
        return new ThermalVideoStreamDto(thermalId, "https://example.com/stream/thermal-001.mp4",
                "{\"zone\":\"battery-pack\",\"frameRate\":15}");
    }

    public ThermalVideoStreamDto create(ThermalVideoStreamDto request) {
        return new ThermalVideoStreamDto(UUID.randomUUID(), request.videoUrl(), request.metadata());
    }

    public ThermalVideoStreamDto update(UUID thermalId, ThermalVideoStreamDto request) {
        return new ThermalVideoStreamDto(thermalId, request.videoUrl(), request.metadata());
    }

    public void delete(UUID thermalId) {
        // TODO: ERD 확정 후 실제 삭제 로직 연결
    }
}
