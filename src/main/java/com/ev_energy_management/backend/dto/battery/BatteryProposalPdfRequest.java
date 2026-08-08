package com.ev_energy_management.backend.dto.battery;

/** 관리자 웹(BatteryDiagnosis.jsx "배터리 매도 제안서" 탭)이 이미 계산해 화면에 표시한 진단
 * 결과. rul-diagnosis의 /report/pdf/full로 그대로 넘기면, 원본 센서값을 다시 돌리지 않고도
 * 매입처 매칭(estimate_offers)·경제성 계산(economics.compute)까지 포함된 정식 문서를 받는다.
 *
 * anthropicApiKey: 매입처 실시간 검색(웹서치)을 개인 키로 한 번만 돌려보고 싶을 때 선택
 * 입력하는 값 — 저장/로그 절대 금지. toString()도 일부러 안 만든다(record 기본
 * toString은 모든 필드를 그대로 찍으므로, 실수로 로그에 이 객체를 찍으면 키가 그대로
 * 남는다 - 아래 커스텀 toString으로 마스킹). */
public record BatteryProposalPdfRequest(
        double capacityKwh,
        String grade,
        double rulCycles,
        double fullLife,
        double healthPct,
        Indicators indicators,
        String anthropicApiKey
) {
    public record Indicators(double life, double capacity, double charge, double stability) {}

    @Override
    public String toString() {
        return "BatteryProposalPdfRequest[capacityKwh=" + capacityKwh + ", grade=" + grade
                + ", rulCycles=" + rulCycles + ", fullLife=" + fullLife + ", healthPct=" + healthPct
                + ", indicators=" + indicators
                + ", anthropicApiKey=" + (anthropicApiKey == null || anthropicApiKey.isBlank() ? "(없음)" : "(입력됨, 마스킹)")
                + "]";
    }
}
