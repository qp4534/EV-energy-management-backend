package com.ev_energy_management.backend.dto.battery;

/** "배터리 잔존가치/판매처" 탭에서 매입처 카드별로 "실시간 검색" 버튼을 눌렀을 때 보내는
 * 요청. serperApiKeyNh/deepseekApiKeyNh는 선택 입력이라 저장/로그 절대 금지 —
 * toString()을 커스텀해서 마스킹한다(레코드 기본 toString은 그대로 찍는다). */
public record BuyerDisclosureRequest(
        String buyerName,
        String serperApiKeyNh,
        String deepseekApiKeyNh
) {
    private static String mask(String key) {
        return key == null || key.isBlank() ? "(없음)" : "(입력됨, 마스킹)";
    }

    @Override
    public String toString() {
        return "BuyerDisclosureRequest[buyerName=" + buyerName
                + ", serperApiKeyNh=" + mask(serperApiKeyNh)
                + ", deepseekApiKeyNh=" + mask(deepseekApiKeyNh)
                + "]";
    }
}
