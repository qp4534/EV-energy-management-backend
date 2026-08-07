package com.ev_energy_management.backend.dto.battery;

import java.util.List;

/** 관리자 웹(BatteryDiagnosis.jsx "배터리 매도 제안서" 탭)이 화면에 이미 표시한 값 그대로.
 * 백엔드/rul-diagnosis 둘 다 재계산하지 않고 그대로 PDF로 렌더링만 하므로,
 * 화면 숫자와 PDF 숫자가 항상 같다. */
public record BatteryProposalPdfRequest(
        String buyerName,
        String buyerRole,
        String buyerLocation,
        double priceTotalManwon,
        double unitPriceWon,
        String negotiationRange,
        String priceGradeLabel,
        String priceNote,
        String grade,
        double remainingCycle,
        double newCycle,
        double healthScorePct,
        List<HealthMetricView> healthMetrics,
        String diagnosisNote,
        List<String> reasons,
        List<String> cautions
) {
    public record HealthMetricView(String label, String score) {}
}
