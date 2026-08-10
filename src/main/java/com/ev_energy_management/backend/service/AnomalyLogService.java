package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.AnomalyLogDto;
import com.ev_energy_management.backend.dto.statsreport.AlertTrendDto;
import com.ev_energy_management.backend.dto.statsreport.FireSummaryStatsDto;
import com.ev_energy_management.backend.entity.AnomalyLogEntity;
import com.ev_energy_management.backend.repository.AnomalyLogRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AnomalyLogService {

    private final AnomalyLogRepository anomalyLogRepository;

    public AnomalyLogService(AnomalyLogRepository anomalyLogRepository) {
        this.anomalyLogRepository = anomalyLogRepository;
    }

    public List<AnomalyLogDto> findAll() {
        return anomalyLogRepository.findAll().stream().map(this::toDto).toList();
    }

    public AnomalyLogDto findById(UUID anomalyId) {
        return toDto(anomalyLogRepository.findById(anomalyId)
                .orElseThrow(() -> new EntityNotFoundException("Anomaly log not found: " + anomalyId)));
    }

    public AnomalyLogDto create(AnomalyLogDto request) {
        AnomalyLogEntity entity = AnomalyLogEntity.builder()
                .abnormalType(request.abnormalType())
                .sourceType(request.sourceType())
                .triggerValue(request.triggerValue())
                .riskLevel(request.riskLevel() != null ? request.riskLevel() : "정상")
                .carId(request.carId())
                .sessionId(request.sessionId())
                .build();
        return toDto(anomalyLogRepository.save(entity));
    }

    public AnomalyLogDto update(UUID anomalyId, AnomalyLogDto request) {
        AnomalyLogEntity entity = anomalyLogRepository.findById(anomalyId)
                .orElseThrow(() -> new EntityNotFoundException("Anomaly log not found: " + anomalyId));
        entity.setAbnormalType(request.abnormalType());
        entity.setSourceType(request.sourceType());
        entity.setTriggerValue(request.triggerValue());
        entity.setRiskLevel(request.riskLevel());
        entity.setCarId(request.carId());
        entity.setSessionId(request.sessionId());
        return toDto(anomalyLogRepository.save(entity));
    }

    public void delete(UUID anomalyId) {
        anomalyLogRepository.deleteById(anomalyId);
    }

    private AnomalyLogDto toDto(AnomalyLogEntity entity) {
        return new AnomalyLogDto(
                entity.getAnomalyId(),
                entity.getAbnormalType(),
                entity.getSourceType(),
                entity.getTriggerValue(),
                entity.getDetectedAt(),
                entity.getRiskLevel(),
                entity.getCarId(),
                entity.getSessionId()
        );
    }

    // 통계/리포트 "화재 예방" 탭
    public FireSummaryStatsDto getFireSummaryStats() {
        List<AnomalyLogEntity> logs = anomalyLogRepository.findAll();

        YearMonth thisMonth = YearMonth.now();
        YearMonth lastMonth = thisMonth.minusMonths(1);

        List<AnomalyLogEntity> thisMonthLogs = logs.stream()
                .filter(a -> a.getDetectedAt() != null && monthOf(a.getDetectedAt()).equals(thisMonth))
                .toList();
        long lastMonthCount = logs.stream()
                .filter(a -> a.getDetectedAt() != null && monthOf(a.getDetectedAt()).equals(lastMonth))
                .count();

        long alertCount = thisMonthLogs.size();
        long alertCountDelta = alertCount - lastMonthCount;

        Map.Entry<String, Long> topType = topEntry(thisMonthLogs, AnomalyLogEntity::getAbnormalType);

        return new FireSummaryStatsDto(
                alertCount,
                alertCountDelta,
                topType == null ? "-" : topType.getKey(),
                topType == null ? 0L : topType.getValue()
        );
    }

    // 통계/리포트 "화재 예방" 탭 - 월별 알림 발생 추이 (최근 7개월)
    // months: 몇 개월치를 볼지 (화재예방 탭 기간 필터 - 3/6/12개월 선택 가능)
    public List<AlertTrendDto> getAlertTrend(int months) {
        List<AnomalyLogEntity> logs = anomalyLogRepository.findAll();
        List<YearMonth> targetMonths = lastNMonths(months);

        List<AlertTrendDto> result = new ArrayList<>();
        for (YearMonth ym : targetMonths) {
            long count = logs.stream()
                    .filter(a -> a.getDetectedAt() != null && monthOf(a.getDetectedAt()).equals(ym))
                    .count();
            result.add(new AlertTrendDto(monthLabel(ym), count));
        }
        return result;
    }

    private List<YearMonth> lastNMonths(int n) {
        YearMonth current = YearMonth.now();
        List<YearMonth> months = new ArrayList<>();
        for (int i = n - 1; i >= 0; i--) {
            months.add(current.minusMonths(i));
        }
        return months;
    }

    private String monthLabel(YearMonth ym) {
        return ym.getMonthValue() + "월";
    }

    private Map.Entry<String, Long> topEntry(List<AnomalyLogEntity> logs,
                                             java.util.function.Function<AnomalyLogEntity, String> getter) {
        return logs.stream()
                .map(getter)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.groupingBy(v -> v, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);
    }

    private YearMonth monthOf(OffsetDateTime dt) {
        return YearMonth.from(dt.atZoneSameInstant(ZoneOffset.systemDefault()));
    }
}