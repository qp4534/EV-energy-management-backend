package com.ev_energy_management.backend.client;

import com.ev_energy_management.backend.dto.battery.BatteryProposalPdfRequest;
import com.ev_energy_management.backend.dto.battery.LiveOffersRequest;
import com.ev_energy_management.backend.exception.AiServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.LinkedHashMap;
import java.util.Map;

/** rul-diagnosis FastAPI 서비스의 /report/pdf/full을 호출한다.
 * Agent1~3을 다시 돌리지 않고 이미 계산된 진단 결과만 넘기지만, 매입처 매칭·경제성 계산·
 * 법적 유의사항까지 포함된 정식 문서를 받는다(예전 /report/pdf/from-view는 화면 값을
 * 그대로 옮겨 적기만 해서 이런 내용이 빠져 있었다). */
@Component
public class RulDiagnosisPdfClient {

    private final RestClient restClient;

    public RulDiagnosisPdfClient(
            @Value("${rul.diagnosis.base-url}") String baseUrl,
            @Value("${rul.diagnosis.connect-timeout-ms:3000}") int connectTimeoutMs,
            @Value("${rul.diagnosis.read-timeout-ms:20000}") int readTimeoutMs
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public byte[] renderProposalPdf(BatteryProposalPdfRequest req) {
        // fastapi(Pydantic)는 snake_case 필드명을 기대해서, 자동 직렬화(camelCase) 대신
        // 여기서 직접 매핑한다. buyer_index/chemistry/new_price_krw는 안 보내면
        // fastapi 쪽 기본값(0번째 매입처/NMC811)이 적용된다.
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("capacity_kwh", req.capacityKwh());
        body.put("grade", req.grade());
        body.put("rul_cycles", req.rulCycles());
        body.put("full_life", req.fullLife());
        body.put("health_pct", req.healthPct());

        Map<String, Object> indicators = new LinkedHashMap<>();
        indicators.put("life", req.indicators().life());
        indicators.put("capacity", req.indicators().capacity());
        indicators.put("charge", req.indicators().charge());
        indicators.put("stability", req.indicators().stability());
        body.put("indicators", indicators);

        try {
            byte[] pdf = restClient.post()
                    .uri("/report/pdf/full")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(byte[].class);
            if (pdf == null || pdf.length == 0) {
                throw new AiServiceUnavailableException("PDF 응답이 비어 있습니다.");
            }
            return pdf;
        } catch (AiServiceUnavailableException e) {
            throw e;
        } catch (RestClientException e) {
            throw new AiServiceUnavailableException("현재 배터리 진단서 PDF 생성 서비스를 사용할 수 없습니다.", e);
        }
    }

    /** 매입처를 실시간 검색으로 찾아서(discover_buyers) 매칭·가격 계산까지 받아온다.
     * Serper/DeepSeek 키는 서버 쪽 시크릿(rul-diagnosis-secret)으로 자동 처리되고,
     * 검색이 실패하거나 서비스가 잠깐 안 되면 예외를 던진다. */
    public Map<String, Object> fetchLiveOffers(LiveOffersRequest req) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("grade", req.grade());
        body.put("capacity_kwh", req.capacityKwh());
        body.put("condition", req.condition());

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> resp = restClient.post()
                    .uri("/buyers/live-offers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            if (resp == null) {
                throw new AiServiceUnavailableException("매입처 조회 응답이 비어 있습니다.");
            }
            return resp;
        } catch (RestClientException e) {
            throw new AiServiceUnavailableException("현재 매입처 실시간 검색 서비스를 사용할 수 없습니다.", e);
        }
    }
}
