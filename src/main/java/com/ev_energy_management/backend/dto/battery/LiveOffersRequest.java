package com.ev_energy_management.backend.dto.battery;

/** "배터리 잔존가치/판매처" 탭이 로드될 때 매입처 목록을 가져오는 요청. 매입처 회사 자체를
 * 검색으로 찾아서 목록을 구성하되, 가격은 그대로 기존 계산식(BNEF/국내 낙찰가 등 출처가
 * 있는 벤치마크)으로 산정한다. Serper/DeepSeek 키는 서버 쪽 시크릿으로 자동 처리되고,
 * 화면에서 키를 입력받지 않는다. */
public record LiveOffersRequest(
        String grade,
        double capacityKwh,
        double condition
) {
}
