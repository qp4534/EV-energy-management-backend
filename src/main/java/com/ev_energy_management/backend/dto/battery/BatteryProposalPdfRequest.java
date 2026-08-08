package com.ev_energy_management.backend.dto.battery;

import java.util.List;
import java.util.Map;

/** 관리자 웹(BatteryDiagnosis.jsx "배터리 매도 제안서" 탭)이 이미 계산해 화면에 표시한 진단
 * 결과. rul-diagnosis의 /report/pdf/full로 그대로 넘기면, 원본 센서값을 다시 돌리지 않고도
 * 매입처 매칭(estimate_offers)·경제성 계산(economics.compute)까지 포함된 정식 문서를 받는다.
 * 매입처 실시간 검색(Serper+DeepSeek)은 서버 쪽 시크릿으로 자동 처리되고, 화면에서 키를
 * 입력받지 않는다.
 *
 * chosenBuyer : "잔존가치/판매처" 탭에서 화면이 이미 고른 매입처(top3 중 하나) - rul-diagnosis의
 *   estimate_offers()/discover_buyers()가 돌려준 offer 객체(한글 키)를 그대로 담아 넘긴다.
 *   null이면 rul-diagnosis가 기존처럼 static BUYERS 1위(buyer_index=0)로 다시 계산한다.
 * reasons : 화면에 이미 표시된 "귀사에 적합한 이유" 추가 문구 - PDF에도 그대로 반영해
 *   화면·PDF 내용이 어긋나지 않게 한다. */
public record BatteryProposalPdfRequest(
        double capacityKwh,
        String grade,
        double rulCycles,
        double fullLife,
        double healthPct,
        Indicators indicators,
        Map<String, Object> chosenBuyer,
        List<String> reasons
) {
    public record Indicators(double life, double capacity, double charge, double stability) {}
}
