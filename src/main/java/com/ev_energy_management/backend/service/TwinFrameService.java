package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.client.FastApiTwinClient;
import com.ev_energy_management.backend.dto.BmsTwinSampleRequest;
import com.ev_energy_management.backend.dto.FastApiTwinFrameResponse;
import com.ev_energy_management.backend.dto.TwinFrameDto;
import com.ev_energy_management.backend.entity.TwinFrameEntity;
import com.ev_energy_management.backend.repository.TwinFrameRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TwinFrameService {

    private final TwinFrameRepository twinFrameRepository;
    private final FastApiTwinClient fastApiTwinClient;

    public TwinFrameService(
            TwinFrameRepository twinFrameRepository,
            FastApiTwinClient fastApiTwinClient
    ) {
        this.twinFrameRepository = twinFrameRepository;
        this.fastApiTwinClient = fastApiTwinClient;
    }

    public List<TwinFrameDto> findAll() {
        return twinFrameRepository.findAll().stream().map(this::toDto).toList();
    }

    public TwinFrameDto findById(UUID frameId) {
        return toDto(twinFrameRepository.findById(frameId)
                .orElseThrow(() -> new EntityNotFoundException("Twin frame not found: " + frameId)));
    }

    public List<TwinFrameDto> findByCarId(UUID carId) {
        return twinFrameRepository.findByCarIdOrderByObservedAtDesc(carId).stream().map(this::toDto).toList();
    }

    public TwinFrameDto create(TwinFrameDto request) {
        TwinFrameEntity entity = TwinFrameEntity.builder()
                .observedAt(request.observedAt())
                .hotspotCellIndex(request.hotspotCellIndex())
                .hotspotConnectorIndex(request.hotspotConnectorIndex())
                .mlRiskLevel(request.mlRiskLevel())
                .physicsRiskLevel(request.physicsRiskLevel())
                .finalRiskLevel(request.finalRiskLevel())
                .imageRiskLevel(request.imageRiskLevel())
                .imageConfidence(request.imageConfidence())
                .rawMetrics(request.rawMetrics())
                .modelInput(request.modelInput())
                .anomalyId(request.anomalyId())
                .carId(request.carId())
                .sessionId(request.sessionId())
                .sourceImageRef(request.sourceImageRef())
                .build();
        return toDto(twinFrameRepository.save(entity));
    }

    /** FastAPI performs inference, live publication, and shared-RDS anomaly persistence. */
    public FastApiTwinFrameResponse evaluateBmsSample(UUID carId, BmsTwinSampleRequest request) {
        validateBmsRequest(carId, request);
        return fastApiTwinClient.evaluate(carId, request);
    }

    public TwinFrameDto update(UUID frameId, TwinFrameDto request) {
        TwinFrameEntity entity = twinFrameRepository.findById(frameId)
                .orElseThrow(() -> new EntityNotFoundException("Twin frame not found: " + frameId));
        entity.setObservedAt(request.observedAt());
        entity.setHotspotCellIndex(request.hotspotCellIndex());
        entity.setHotspotConnectorIndex(request.hotspotConnectorIndex());
        entity.setMlRiskLevel(request.mlRiskLevel());
        entity.setPhysicsRiskLevel(request.physicsRiskLevel());
        entity.setFinalRiskLevel(request.finalRiskLevel());
        entity.setImageRiskLevel(request.imageRiskLevel());
        entity.setImageConfidence(request.imageConfidence());
        entity.setRawMetrics(request.rawMetrics());
        entity.setModelInput(request.modelInput());
        entity.setAnomalyId(request.anomalyId());
        entity.setCarId(request.carId());
        entity.setSessionId(request.sessionId());
        entity.setSourceImageRef(request.sourceImageRef());
        return toDto(twinFrameRepository.save(entity));
    }

    public void delete(UUID frameId) {
        twinFrameRepository.deleteById(frameId);
    }

    private TwinFrameDto toDto(TwinFrameEntity entity) {
        return new TwinFrameDto(
                entity.getFrameId(),
                entity.getObservedAt(),
                entity.getHotspotCellIndex(),
                entity.getHotspotConnectorIndex(),
                entity.getMlRiskLevel(),
                entity.getPhysicsRiskLevel(),
                entity.getFinalRiskLevel(),
                entity.getImageRiskLevel(),
                entity.getImageConfidence(),
                entity.getRawMetrics(),
                entity.getModelInput(),
                entity.getAnomalyId(),
                entity.getCarId(),
                entity.getSessionId(),
                entity.getSourceImageRef()
        );
    }

    private static void validateBmsRequest(UUID carId, BmsTwinSampleRequest request) {
        if (carId == null || request == null || request.observedAt() == null || request.sequence() == null) {
            throw new IllegalArgumentException("carId, observedAt, request, and sequence are required");
        }
        if (request.sequence() < 0) {
            throw new IllegalArgumentException("sequence must be non-negative");
        }
        if (request.temperatureDecic() == null || request.temperatureDecic().size() != 96) {
            throw new IllegalArgumentException("temperature_decic must contain 96 values");
        }
        if (request.voltageMv() == null || request.voltageMv().size() != 96) {
            throw new IllegalArgumentException("voltage_mv must contain 96 values");
        }
        if (request.connectorTemperatureDecic() == null || request.connectorTemperatureDecic().size() != 3) {
            throw new IllegalArgumentException("connector_temperature_decic must contain 3 values");
        }
    }
}
