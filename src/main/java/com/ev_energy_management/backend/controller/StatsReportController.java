package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.statsreport.AlertTrendDto;
import com.ev_energy_management.backend.dto.statsreport.BatteryDiagnosisTrendDto;
import com.ev_energy_management.backend.dto.statsreport.BatteryGradeDistributionDto;
import com.ev_energy_management.backend.dto.statsreport.BatteryMetricAverageDto;
import com.ev_energy_management.backend.dto.statsreport.BatterySohTrendDto;
import com.ev_energy_management.backend.dto.statsreport.FireSummaryStatsDto;
import com.ev_energy_management.backend.dto.statsreport.MemberTrendDto;
import com.ev_energy_management.backend.dto.statsreport.RecentDiagnosisDto;
import com.ev_energy_management.backend.dto.statsreport.UserSummaryStatsDto;
import com.ev_energy_management.backend.dto.statsreport.UserTypeDistributionDto;
import com.ev_energy_management.backend.service.AnomalyLogService;
import com.ev_energy_management.backend.service.BatteryDiagnosisMetricService;
import com.ev_energy_management.backend.service.BatteryPassportService;
import com.ev_energy_management.backend.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 통계/리포트(StatsReport.jsx)
@RestController
@RequestMapping("/api/stats-report")
public class StatsReportController {

    private final UserService userService;
    private final BatteryDiagnosisMetricService batteryDiagnosisMetricService;
    private final BatteryPassportService batteryPassportService;
    private final AnomalyLogService anomalyLogService;

    public StatsReportController(UserService userService,
                                 BatteryDiagnosisMetricService batteryDiagnosisMetricService,
                                 BatteryPassportService batteryPassportService,
                                 AnomalyLogService anomalyLogService) {
        this.userService = userService;
        this.batteryDiagnosisMetricService = batteryDiagnosisMetricService;
        this.batteryPassportService = batteryPassportService;
        this.anomalyLogService = anomalyLogService;
    }

    // "이용자" 탭
    @GetMapping("/user/type-distribution")
    public List<UserTypeDistributionDto> getUserTypeDistribution() {
        return userService.getUserTypeDistribution();
    }

    @GetMapping("/user/member-trend")
    public List<MemberTrendDto> getMemberTrend() {
        return userService.getMemberTrend();
    }

    @GetMapping("/user/summary")
    public UserSummaryStatsDto getUserSummaryStats() {
        return userService.getUserSummaryStats();
    }

    // "배터리 진단" 탭
    @GetMapping("/battery/diagnosis-trend")
    public List<BatteryDiagnosisTrendDto> getBatteryDiagnosisTrend() {
        return batteryDiagnosisMetricService.getDiagnosisTrend();
    }

    @GetMapping("/battery/soh-trend")
    public List<BatterySohTrendDto> getBatterySohTrend() {
        return batteryPassportService.getSohTrend();
    }

    @GetMapping("/battery/grade-distribution")
    public List<BatteryGradeDistributionDto> getBatteryGradeDistribution() {
        return batteryPassportService.getGradeDistribution();
    }

    // "배터리 처리" 카드 자리를 대체 - 전체 배터리 진단 지표 평균
    @GetMapping("/battery/metric-average")
    public BatteryMetricAverageDto getBatteryMetricAverage() {
        return batteryDiagnosisMetricService.getMetricAverage();
    }

    // "최근 처리 이력" 표 자리를 대체 - 최근 진단 이력 (기본 6건)
    @GetMapping("/battery/recent-diagnoses")
    public List<RecentDiagnosisDto> getRecentDiagnoses(@RequestParam(defaultValue = "6") int limit) {
        return batteryPassportService.getRecentDiagnoses(limit);
    }

    // "화재 예방" 탭
    @GetMapping("/fire/summary")
    public FireSummaryStatsDto getFireSummaryStats() {
        return anomalyLogService.getFireSummaryStats();
    }

    @GetMapping("/fire/alert-trend")
    public List<AlertTrendDto> getAlertTrend(@RequestParam(defaultValue = "6") int months) {
        return anomalyLogService.getAlertTrend(months);
    }
}