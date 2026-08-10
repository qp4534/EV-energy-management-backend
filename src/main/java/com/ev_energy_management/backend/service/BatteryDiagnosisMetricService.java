package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.BatteryDiagnosisMetricDto;
import com.ev_energy_management.backend.dto.statsreport.BatteryDiagnosisTrendDto;
import com.ev_energy_management.backend.dto.statsreport.BatteryMetricAverageDto;
import com.ev_energy_management.backend.entity.BatteryDiagnosisMetricEntity;
import com.ev_energy_management.backend.repository.BatteryDiagnosisMetricRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BatteryDiagnosisMetricService {

    private final BatteryDiagnosisMetricRepository batteryDiagnosisMetricRepository;

    public BatteryDiagnosisMetricService(BatteryDiagnosisMetricRepository batteryDiagnosisMetricRepository) {
        this.batteryDiagnosisMetricRepository = batteryDiagnosisMetricRepository;
    }

    public List<BatteryDiagnosisMetricDto> findAll() {
        return batteryDiagnosisMetricRepository.findAll().stream().map(this::toDto).toList();
    }

    public BatteryDiagnosisMetricDto findById(UUID metricId) {
        return toDto(batteryDiagnosisMetricRepository.findById(metricId)
                .orElseThrow(() -> new EntityNotFoundException("Battery diagnosis metric not found: " + metricId)));
    }

    public BatteryDiagnosisMetricDto create(BatteryDiagnosisMetricDto request) {
        BatteryDiagnosisMetricEntity entity = BatteryDiagnosisMetricEntity.builder()
                .remainingLifeScore(request.remainingLifeScore())
                .dischargePowerScore(request.dischargePowerScore())
                .chargeHealthScore(request.chargeHealthScore())
                .voltageStabilityScore(request.voltageStabilityScore())
                .batteryId(request.batteryId())
                .build();
        return toDto(batteryDiagnosisMetricRepository.save(entity));
    }

    public BatteryDiagnosisMetricDto update(UUID metricId, BatteryDiagnosisMetricDto request) {
        BatteryDiagnosisMetricEntity entity = batteryDiagnosisMetricRepository.findById(metricId)
                .orElseThrow(() -> new EntityNotFoundException("Battery diagnosis metric not found: " + metricId));
        entity.setRemainingLifeScore(request.remainingLifeScore());
        entity.setDischargePowerScore(request.dischargePowerScore());
        entity.setChargeHealthScore(request.chargeHealthScore());
        entity.setVoltageStabilityScore(request.voltageStabilityScore());
        entity.setBatteryId(request.batteryId());
        return toDto(batteryDiagnosisMetricRepository.save(entity));
    }

    public void delete(UUID metricId) {
        batteryDiagnosisMetricRepository.deleteById(metricId);
    }

    private BatteryDiagnosisMetricDto toDto(BatteryDiagnosisMetricEntity entity) {
        return new BatteryDiagnosisMetricDto(
                entity.getMetricId(),
                entity.getRemainingLifeScore(),
                entity.getDischargePowerScore(),
                entity.getChargeHealthScore(),
                entity.getVoltageStabilityScore(),
                entity.getDiagnosedAt(),
                entity.getBatteryId()
        );
    }

    // 통계/리포트 "배터리 진단" 탭 - 월별 진단 건수 (최근 7개월)
    public List<BatteryDiagnosisTrendDto> getDiagnosisTrend() {
        List<BatteryDiagnosisMetricEntity> metrics = batteryDiagnosisMetricRepository.findAll();
        List<YearMonth> months = lastNMonths(7);

        List<BatteryDiagnosisTrendDto> result = new ArrayList<>();
        for (YearMonth ym : months) {
            long count = metrics.stream()
                    .filter(m -> m.getDiagnosedAt() != null
                            && YearMonth.from(m.getDiagnosedAt().atZoneSameInstant(ZoneOffset.systemDefault())).equals(ym))
                    .count();
            result.add(new BatteryDiagnosisTrendDto(monthLabel(ym), count));
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

    // 통계/리포트 "배터리 진단" 탭 - 전체 배터리 진단 지표 평균
    public BatteryMetricAverageDto getMetricAverage() {
        List<BatteryDiagnosisMetricEntity> metrics = batteryDiagnosisMetricRepository.findAll();
        if (metrics.isEmpty()) {
            return new BatteryMetricAverageDto(0.0, 0.0, 0.0, 0.0);
        }
        double remainingLifeAvg = avg(metrics, BatteryDiagnosisMetricEntity::getRemainingLifeScore);
        double dischargePowerAvg = avg(metrics, BatteryDiagnosisMetricEntity::getDischargePowerScore);
        double chargeHealthAvg = avg(metrics, BatteryDiagnosisMetricEntity::getChargeHealthScore);
        double voltageStabilityAvg = avg(metrics, BatteryDiagnosisMetricEntity::getVoltageStabilityScore);
        return new BatteryMetricAverageDto(remainingLifeAvg, dischargePowerAvg, chargeHealthAvg, voltageStabilityAvg);
    }

    private double avg(List<BatteryDiagnosisMetricEntity> metrics,
                       java.util.function.Function<BatteryDiagnosisMetricEntity, Integer> getter) {
        double avg = metrics.stream()
                .map(getter)
                .filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);
        return Math.round(avg * 10) / 10.0;
    }
}
