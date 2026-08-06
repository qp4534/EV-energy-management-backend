package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.AiReportDto;
import com.ev_energy_management.backend.entity.AiReportEntity;
import com.ev_energy_management.backend.repository.AiReportRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class AiReportService {

    private static final TypeReference<Map<String, Object>> REPORT_DATA_TYPE =
            new TypeReference<>() {};

    private final AiReportRepository aiReportRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiReportService(AiReportRepository aiReportRepository) {
        this.aiReportRepository = aiReportRepository;
    }

    public List<AiReportDto> findAll() {
        return aiReportRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDto)
                .toList();
    }

    public AiReportDto findById(UUID reportId) {
        return toDto(aiReportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("AI report not found: " + reportId)));
    }

    public AiReportDto create(AiReportDto request) {
        AiReportEntity entity = AiReportEntity.builder()
                .title(request.title())
                .reportData(writeReportData(request.reportData()))
                .reportType(normalizeReportType(request.reportType()))
                .carId(request.carId())
                .anomalyId(request.anomalyId())
                .isRead(false)
                .build();
        return toDto(aiReportRepository.save(entity));
    }

    public AiReportDto update(UUID reportId, AiReportDto request) {
        AiReportEntity entity = aiReportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("AI report not found: " + reportId));
        if (request.title() != null) {
            entity.setTitle(request.title());
        }
        if (request.reportData() != null) {
            entity.setReportData(writeReportData(request.reportData()));
        }
        if (request.reportType() != null) {
            entity.setReportType(normalizeReportType(request.reportType()));
        }
        if (request.carId() != null) {
            entity.setCarId(request.carId());
        }
        if (request.anomalyId() != null) {
            entity.setAnomalyId(request.anomalyId());
        }
        if (request.isRead() != null) {
            entity.setIsRead(request.isRead());
        }
        return toDto(aiReportRepository.save(entity));
    }

    public AiReportDto markAsRead(UUID reportId) {
        AiReportEntity entity = aiReportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("AI report not found: " + reportId));
        if (!Boolean.TRUE.equals(entity.getIsRead())) {
            entity.setIsRead(true);
            entity = aiReportRepository.save(entity);
        }
        return toDto(entity);
    }

    public void delete(UUID reportId) {
        aiReportRepository.deleteById(reportId);
    }

    private AiReportDto toDto(AiReportEntity entity) {
        return new AiReportDto(
                entity.getReportId(),
                entity.getTitle(),
                readReportData(entity.getReportData()),
                normalizeReportType(entity.getReportType()),
                entity.getCreatedAt(),
                entity.getCarId(),
                entity.getAnomalyId(),
                entity.getIsRead()
        );
    }

    private Map<String, Object> readReportData(String value) {
        if (value == null || value.isBlank()) {
            return Map.of();
        }
        try {
            Map<String, Object> parsed = objectMapper.readValue(value, REPORT_DATA_TYPE);
            if (parsed.containsKey("sections")) {
                return parsed;
            }
            Object summary = parsed.get("summary");
            if (summary instanceof String summaryText && !summaryText.isBlank()) {
                return legacyReportData(summaryText);
            }
            return parsed;
        } catch (JsonProcessingException ignored) {
            return legacyReportData(value);
        }
    }

    private Map<String, Object> legacyReportData(String content) {
        Map<String, Object> section = new LinkedHashMap<>();
        section.put("type", "summary");
        section.put("title", "보고서 요약");
        section.put("content", content);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("schemaVersion", "legacy");
        report.put("isAiGenerated", true);
        report.put("riskLevel", "UNKNOWN");
        report.put("sections", List.of(section));
        report.put("sources", List.of());
        report.put("missingFields", List.of());
        report.put("actions", List.of());
        return report;
    }

    private String writeReportData(Map<String, Object> reportData) {
        try {
            return objectMapper.writeValueAsString(reportData == null ? Map.of() : reportData);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("AI report data must be valid JSON", e);
        }
    }

    private String normalizeReportType(String value) {
        if (value == null || value.isBlank()) {
            return "월간보고서";
        }
        String normalized = value.trim();
        return switch (normalized.toUpperCase(Locale.ROOT)) {
            case "MONTHLY", "월간", "월간보고서" -> "월간보고서";
            case "ANOMALY", "이상", "이상보고서" -> "이상";
            default -> normalized;
        };
    }
}
