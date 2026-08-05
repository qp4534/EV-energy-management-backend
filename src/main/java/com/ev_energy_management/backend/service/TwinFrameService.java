package com.ev_energy_management.backend.service;

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

    public TwinFrameService(TwinFrameRepository twinFrameRepository) {
        this.twinFrameRepository = twinFrameRepository;
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
                .anomalyId(request.anomalyId())
                .carId(request.carId())
                .sourceImageRef(request.sourceImageRef())
                .build();
        return toDto(twinFrameRepository.save(entity));
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
        entity.setAnomalyId(request.anomalyId());
        entity.setCarId(request.carId());
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
                entity.getAnomalyId(),
                entity.getCarId(),
                entity.getSourceImageRef()
        );
    }
}
