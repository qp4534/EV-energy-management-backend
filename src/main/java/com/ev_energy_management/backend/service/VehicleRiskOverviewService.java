package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.VehicleRiskOverviewDto;
import com.ev_energy_management.backend.repository.AnomalyLogRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;

@Service
public class VehicleRiskOverviewService {

    private static final String NORMAL = "정상";
    private static final String CAUTION = "주의";
    private static final String WARNING = "경고";
    private static final String EMERGENCY = "긴급";

    private final AnomalyLogRepository anomalyLogRepository;

    public VehicleRiskOverviewService(AnomalyLogRepository anomalyLogRepository) {
        this.anomalyLogRepository = anomalyLogRepository;
    }

    public VehicleRiskOverviewDto getOverview() {
        List<VehicleRiskOverviewDto.VehicleRiskDto> vehicles = anomalyLogRepository.findLatestRiskByCar().stream()
                .map(row -> new VehicleRiskOverviewDto.VehicleRiskDto(
                        row.getCarId(),
                        row.getCarNumber(),
                        row.getModel(),
                        row.getVin(),
                        normalizeRiskLevel(row.getRiskLevel()),
                        row.getAbnormalType(),
                        toOffsetDateTime(row.getDetectedAt())
                ))
                .toList();

        long emergency = countByRisk(vehicles, EMERGENCY);
        long warning = countByRisk(vehicles, WARNING);
        long caution = countByRisk(vehicles, CAUTION);
        long normal = vehicles.size() - emergency - warning - caution;

        List<VehicleRiskOverviewDto.DailyRiskCountDto> dailyRiskCounts = anomalyLogRepository
                .findRecentDailyRiskCounts().stream()
                .map(row -> new VehicleRiskOverviewDto.DailyRiskCountDto(row.getDate(), row.getCount()))
                .sorted(Comparator.comparing(VehicleRiskOverviewDto.DailyRiskCountDto::date))
                .toList();

        return new VehicleRiskOverviewDto(
                new VehicleRiskOverviewDto.VehicleRiskSummaryDto(
                        vehicles.size(), emergency, warning, caution, normal
                ),
                vehicles,
                dailyRiskCounts
        );
    }

    private long countByRisk(List<VehicleRiskOverviewDto.VehicleRiskDto> vehicles, String riskLevel) {
        return vehicles.stream().filter(vehicle -> riskLevel.equals(vehicle.riskLevel())).count();
    }

    private String normalizeRiskLevel(String riskLevel) {
        if (riskLevel == null) {
            return NORMAL;
        }
        return switch (riskLevel) {
            case CAUTION, WARNING, EMERGENCY -> riskLevel;
            default -> NORMAL;
        };
    }

    private OffsetDateTime toOffsetDateTime(Instant detectedAt) {
        return detectedAt == null ? null : detectedAt.atOffset(ZoneOffset.UTC);
    }
}
