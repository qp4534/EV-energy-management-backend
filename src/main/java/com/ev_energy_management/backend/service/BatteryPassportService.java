package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.BatteryPassportDto;
import com.ev_energy_management.backend.dto.statsreport.BatteryGradeDistributionDto;
import com.ev_energy_management.backend.dto.statsreport.BatterySohTrendDto;
import com.ev_energy_management.backend.dto.statsreport.RecentDiagnosisDto;
import com.ev_energy_management.backend.entity.BatteryPassportEntity;
import com.ev_energy_management.backend.repository.BatteryPassportRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BatteryPassportService {

    private final BatteryPassportRepository batteryPassportRepository;

    public BatteryPassportService(BatteryPassportRepository batteryPassportRepository) {
        this.batteryPassportRepository = batteryPassportRepository;
    }

    public List<BatteryPassportDto> findAll() {
        return batteryPassportRepository.findAll().stream().map(this::toDto).toList();
    }

    public BatteryPassportDto findById(UUID batteryId) {
        return toDto(batteryPassportRepository.findById(batteryId)
                .orElseThrow(() -> new EntityNotFoundException("Battery passport not found: " + batteryId)));
    }

    public BatteryPassportDto findByCarId(UUID carId) {
        return toDto(batteryPassportRepository.findByCarId(carId)
                .orElseThrow(() -> new EntityNotFoundException("Battery passport not found for car: " + carId)));
    }

    public BatteryPassportDto create(BatteryPassportDto request) {
        BatteryPassportEntity entity = BatteryPassportEntity.builder()
                .manufacturer(request.manufacturer())
                .batteryType(request.batteryType())
                .ratedCapacity(request.ratedCapacity())
                .sohScore(request.sohScore())
                .chargeCycles(request.chargeCycles())
                .currentTemp(request.currentTemp())
                .lastInspectedAt(request.lastInspectedAt())
                .carId(request.carId())
                .batteryLevel(request.batteryLevel() != null ? request.batteryLevel() : "미등록")
                .remainingCycles(request.remainingCycles())
                .totalCycles(request.totalCycles())
                .reuseStatus(request.reuseStatus())
                .gradeDetail(request.gradeDetail())
                .reliabilityScore(request.reliabilityScore())
                .reuseProbabilities(request.reuseProbabilities())
                .voltage(request.voltage())
                .current(request.current())
                .rul(request.rul())
                .manufacturedAt(request.manufacturedAt())
                .installedAt(request.installedAt())
                .build();
        return toDto(batteryPassportRepository.save(entity));
    }

    public BatteryPassportDto update(UUID batteryId, BatteryPassportDto request) {
        BatteryPassportEntity entity = batteryPassportRepository.findById(batteryId)
                .orElseThrow(() -> new EntityNotFoundException("Battery passport not found: " + batteryId));
        entity.setManufacturer(request.manufacturer());
        entity.setBatteryType(request.batteryType());
        entity.setRatedCapacity(request.ratedCapacity());
        entity.setSohScore(request.sohScore());
        entity.setChargeCycles(request.chargeCycles());
        entity.setCurrentTemp(request.currentTemp());
        entity.setLastInspectedAt(request.lastInspectedAt());
        entity.setCarId(request.carId());
        entity.setBatteryLevel(request.batteryLevel());
        entity.setRemainingCycles(request.remainingCycles());
        entity.setTotalCycles(request.totalCycles());
        entity.setReuseStatus(request.reuseStatus());
        entity.setGradeDetail(request.gradeDetail());
        entity.setReliabilityScore(request.reliabilityScore());
        entity.setReuseProbabilities(request.reuseProbabilities());
        entity.setVoltage(request.voltage());
        entity.setCurrent(request.current());
        entity.setRul(request.rul());
        entity.setManufacturedAt(request.manufacturedAt());
        entity.setInstalledAt(request.installedAt());
        return toDto(batteryPassportRepository.save(entity));
    }

    public void delete(UUID batteryId) {
        batteryPassportRepository.deleteById(batteryId);
    }

    private BatteryPassportDto toDto(BatteryPassportEntity entity) {
        return new BatteryPassportDto(
                entity.getBatteryId(),
                entity.getManufacturer(),
                entity.getBatteryType(),
                entity.getRatedCapacity(),
                entity.getSohScore(),
                entity.getChargeCycles(),
                entity.getCurrentTemp(),
                entity.getLastInspectedAt(),
                entity.getCarId(),
                entity.getBatteryLevel(),
                entity.getRemainingCycles(),
                entity.getTotalCycles(),
                entity.getReuseStatus(),
                entity.getGradeDetail(),
                entity.getReliabilityScore(),
                entity.getReuseProbabilities(),
                entity.getVoltage(),
                entity.getCurrent(),
                entity.getRul(),
                entity.getManufacturedAt(),
                entity.getInstalledAt()
        );
    }

    // 통계/리포트 "배터리 진단" 탭 - 등급별 분포 (reuse_status 기준: 양호/노후/수명말기)
    public List<BatteryGradeDistributionDto> getGradeDistribution() {
        List<BatteryPassportEntity> batteries = batteryPassportRepository.findAll();
        Map<String, Long> counts = batteries.stream()
                .filter(b -> b.getReuseStatus() != null)
                .collect(Collectors.groupingBy(BatteryPassportEntity::getReuseStatus, Collectors.counting()));

        // RiskLevelCard 색상 타입(normal=양호/caution=노후/emergency=수명말기)에 맞춰 key 부여
        return List.of(
                new BatteryGradeDistributionDto("normal", "양호", counts.getOrDefault("양호", 0L)),
                new BatteryGradeDistributionDto("caution", "노후", counts.getOrDefault("노후", 0L)),
                new BatteryGradeDistributionDto("emergency", "수명말기", counts.getOrDefault("수명말기", 0L))
        );
    }

    // 통계/리포트 "배터리 진단" 탭 - 평균 SOH 추이
    // TEMP: 진짜 월별 SOH 이력이 없어서, "그 달에 마지막 점검(last_inspected_at)한 배터리들의
    // 현재 soh_score 평균"으로 근사함. 정확한 시계열이 필요하면 SOH 이력 테이블이 별도로 있어야 함.
    public List<BatterySohTrendDto> getSohTrend() {
        List<BatteryPassportEntity> batteries = batteryPassportRepository.findAll();
        List<YearMonth> months = lastNMonths(7);

        List<BatterySohTrendDto> result = new ArrayList<>();
        for (YearMonth ym : months) {
            List<BigDecimal> sohValues = batteries.stream()
                    .filter(b -> b.getLastInspectedAt() != null
                            && YearMonth.from(b.getLastInspectedAt()).equals(ym)
                            && b.getSohScore() != null)
                    .map(BatteryPassportEntity::getSohScore)
                    .toList();
            double avg = sohValues.isEmpty() ? 0
                    : sohValues.stream().mapToDouble(BigDecimal::doubleValue).average().orElse(0);
            result.add(new BatterySohTrendDto(monthLabel(ym), Math.round(avg * 10) / 10.0));
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

    // 통계/리포트 "배터리 진단" 탭
    // 점검일(last_inspected_at) 최신순으로 상위 N건
    public List<RecentDiagnosisDto> getRecentDiagnoses(int limit) {
        List<BatteryPassportEntity> batteries = batteryPassportRepository.findAll();
        return batteries.stream()
                .filter(b -> b.getLastInspectedAt() != null)
                .sorted(Comparator.comparing(BatteryPassportEntity::getLastInspectedAt).reversed())
                .limit(limit)
                .map(b -> new RecentDiagnosisDto(
                        shortBatteryId(b.getBatteryId()),
                        b.getGradeDetail(),
                        b.getSohScore() == null ? null : b.getSohScore().doubleValue(),
                        b.getLastInspectedAt()
                ))
                .toList();
    }

    // UUID 그대로 보여주면 너무 길어서, "BT-XXXX" 형태의 짧은 표시용 ID로 변환
    private String shortBatteryId(UUID batteryId) {
        return "BT-" + batteryId.toString().substring(0, 4).toUpperCase();
    }
}
