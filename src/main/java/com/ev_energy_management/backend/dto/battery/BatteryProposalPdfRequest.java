package com.ev_energy_management.backend.dto.battery;

/** 관리자 웹(BatteryDiagnosis.jsx "배터리 매도 제안서" 탭)이 이미 계산해 화면에 표시한 진단
 * 결과. rul-diagnosis의 /report/pdf/full로 그대로 넘기면, 원본 센서값을 다시 돌리지 않고도
 * 매입처 매칭(estimate_offers)·경제성 계산(economics.compute)까지 포함된 정식 문서를 받는다. */
public record BatteryProposalPdfRequest(
        double capacityKwh,
        String grade,
        double rulCycles,
        double fullLife,
        double healthPct,
        Indicators indicators
) {
    public record Indicators(double life, double capacity, double charge, double stability) {}
}
