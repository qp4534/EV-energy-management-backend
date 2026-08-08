package com.ev_energy_management.backend.dto.battery;

/** "배터리 잔존가치/판매처" 탭에서 "실시간 검색으로 매입처 확인"을 눌렀을 때 보내는 요청.
 * 매입처 회사 자체를 검색으로 찾아서 목록을 다시 구성하되, 가격은 그대로 기존 계산식
 * (BNEF/국내 낙찰가 등 출처가 있는 벤치마크)으로 산정한다. serperApiKeyNh/deepseekApiKeyNh는
 * 선택 입력이라 저장/로그 절대 금지. */
public record LiveOffersRequest(
        String grade,
        double capacityKwh,
        double condition,
        String serperApiKeyNh,
        String deepseekApiKeyNh
) {
    private static String mask(String key) {
        return key == null || key.isBlank() ? "(없음)" : "(입력됨, 마스킹)";
    }

    @Override
    public String toString() {
        return "LiveOffersRequest[grade=" + grade + ", capacityKwh=" + capacityKwh
                + ", condition=" + condition
                + ", serperApiKeyNh=" + mask(serperApiKeyNh)
                + ", deepseekApiKeyNh=" + mask(deepseekApiKeyNh)
                + "]";
    }
}
