package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.AiReportDto;
import com.ev_energy_management.backend.entity.AiReportEntity;
import com.ev_energy_management.backend.repository.AiReportRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AiReportService {

    private final AiReportRepository aiReportRepository;

    public AiReportService(AiReportRepository aiReportRepository) {
        this.aiReportRepository = aiReportRepository;
    }

    public List<AiReportDto> findAll() {
        return aiReportRepository.findAll().stream().map(this::toDto).toList();
    }

    public AiReportDto findById(UUID reportId) {
        return toDto(aiReportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("AI report not found: " + reportId)));
    }

    public AiReportDto create(AiReportDto request) {
        AiReportEntity entity = AiReportEntity.builder()
                .title(request.title())
                .reportData(request.reportData())
                .reportType(request.reportType() != null ? request.reportType() : "월간")
                .carId(request.carId())
                .anomalyId(request.anomalyId())
                .isRead(false)
                .build();
        return toDto(aiReportRepository.save(entity));
    }

    public AiReportDto update(UUID reportId, AiReportDto request) {
        AiReportEntity entity = aiReportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("AI report not found: " + reportId));
        entity.setTitle(request.title());
        entity.setReportData(request.reportData());
        entity.setReportType(request.reportType());
        entity.setCarId(request.carId());
        entity.setAnomalyId(request.anomalyId());
        entity.setIsRead(request.isRead());
        return toDto(aiReportRepository.save(entity));
    }

    public void delete(UUID reportId) {
        aiReportRepository.deleteById(reportId);
    }

    private AiReportDto toDto(AiReportEntity entity) {
        return new AiReportDto(
                entity.getReportId(),
                entity.getTitle(),
                entity.getReportData(),
                entity.getReportType(),
                entity.getCreatedAt(),
                entity.getCarId(),
                entity.getAnomalyId(),
                entity.getIsRead()
        );
    }
}
