package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.AiReportDto;
import com.ev_energy_management.backend.dto.NotificationCreateRequest;
import com.ev_energy_management.backend.entity.AiReportEntity;
import com.ev_energy_management.backend.entity.CarEntity;
import com.ev_energy_management.backend.repository.AiReportRepository;
import com.ev_energy_management.backend.repository.CarRepository;
import com.ev_energy_management.backend.security.AuthenticatedUser;
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
    private final CarRepository carRepository;
    private final NotificationService notificationService;
    private final ActionLogWriter actionLogWriter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiReportService(
            AiReportRepository aiReportRepository,
            CarRepository carRepository,
            NotificationService notificationService,
            ActionLogWriter actionLogWriter
    ) {
        this.aiReportRepository = aiReportRepository;
        this.carRepository = carRepository;
        this.notificationService = notificationService;
        this.actionLogWriter = actionLogWriter;
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
        AiReportDto saved = toDto(aiReportRepository.save(entity));
        notifyCarOwner(saved, saved.title(), "새 AI 보고서가 도착했습니다.");
        return saved;
    }

    // AI 보고서 상세 화면(frontend-web)의 "고객 알림 발송" 액션. create() 시점에 이미 한 번
    // 자동으로 알림이 가지만, 관제자가 고객이 확인했는지 못 미더울 때 수동으로 다시 보낼 수 있게
    // 별도로 둔다.
    public void notifyCustomer(UUID reportId) {
        AiReportEntity entity = aiReportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("AI report not found: " + reportId));
        notifyCarOwner(toDto(entity), entity.getTitle(), "차량 상태를 확인해주세요.");
    }

    // AI 보고서 상세 화면의 "긴급출동 배차" 액션. 전 직원이 아니라 해당 차주에게만 알림을 보내고
    // (알림 피로 방지 - 다른 직원은 필요하면 ACTION_LOGS로 조회), 배차 이력은 감사 로그로 남긴다.
    public void dispatchEmergency(AuthenticatedUser actor, UUID reportId) {
        AiReportEntity entity = aiReportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("AI report not found: " + reportId));
        boolean notified = notifyCarOwner(
                toDto(entity),
                "긴급출동이 접수되었습니다",
                "안전을 위해 즉시 차량에서 벗어나 주세요. 긴급출동팀이 곧 도착합니다.",
                "긴급"
        );
        if (notified) {
            actionLogWriter.write(
                    actor == null ? null : actor.userId(),
                    "EMERGENCY_DISPATCH",
                    "AI_REPORTS",
                    reportId,
                    Map.of("carId", entity.getCarId() == null ? "" : entity.getCarId().toString())
            );
        }
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

    private boolean notifyCarOwner(AiReportDto report, String title, String body) {
        String riskLevel = "이상".equals(report.reportType()) ? "경고" : "정상";
        return notifyCarOwner(report, title, body, riskLevel);
    }

    private boolean notifyCarOwner(AiReportDto report, String title, String body, String riskLevel) {
        if (report.carId() == null) return false;
        CarEntity car = carRepository.findById(report.carId()).orElse(null);
        if (car == null) return false;
        notificationService.create(
                car.getUserId(),
                new NotificationCreateRequest(riskLevel, title, body, report.carId(), report.reportId())
        );
        return true;
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
