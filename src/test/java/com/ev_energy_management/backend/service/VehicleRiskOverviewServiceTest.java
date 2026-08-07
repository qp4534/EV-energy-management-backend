package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.VehicleRiskOverviewDto;
import com.ev_energy_management.backend.repository.AnomalyLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleRiskOverviewServiceTest {

    @Mock
    private AnomalyLogRepository anomalyLogRepository;

    @Test
    void summarizesLatestRiskAndDefaultsMissingOrUnknownRiskToNormal() {
        when(anomalyLogRepository.findLatestRiskByCar()).thenReturn(List.of(
                vehicle("긴급"), vehicle("경고"), vehicle("주의"), vehicle("정상"), vehicle(null), vehicle("알수없음")
        ));
        when(anomalyLogRepository.findRecentDailyRiskCounts()).thenReturn(List.of(
                daily("2026-08-07", 3L), daily("2026-08-06", 1L)
        ));

        VehicleRiskOverviewDto overview = new VehicleRiskOverviewService(anomalyLogRepository).getOverview();

        assertThat(overview.summary().total()).isEqualTo(6);
        assertThat(overview.summary().emergency()).isEqualTo(1);
        assertThat(overview.summary().warning()).isEqualTo(1);
        assertThat(overview.summary().caution()).isEqualTo(1);
        assertThat(overview.summary().normal()).isEqualTo(3);
        assertThat(overview.vehicles().get(4).riskLevel()).isEqualTo("정상");
        assertThat(overview.dailyRiskCounts()).extracting(VehicleRiskOverviewDto.DailyRiskCountDto::date)
                .containsExactly("2026-08-06", "2026-08-07");
    }

    private AnomalyLogRepository.VehicleRiskProjection vehicle(String riskLevel) {
        return new AnomalyLogRepository.VehicleRiskProjection() {
            @Override public UUID getCarId() { return UUID.randomUUID(); }
            @Override public String getCarNumber() { return "12가1234"; }
            @Override public String getModel() { return "EV"; }
            @Override public String getVin() { return "VIN"; }
            @Override public String getRiskLevel() { return riskLevel; }
            @Override public String getAbnormalType() { return "테스트"; }
            @Override public Instant getDetectedAt() { return Instant.parse("2026-08-07T01:00:00Z"); }
        };
    }

    private AnomalyLogRepository.DailyRiskCountProjection daily(String date, Long count) {
        return new AnomalyLogRepository.DailyRiskCountProjection() {
            @Override public String getDate() { return date; }
            @Override public Long getCount() { return count; }
        };
    }
}
