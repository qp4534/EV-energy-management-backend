package com.ev_energy_management.backend.client;

import com.ev_energy_management.backend.dto.battery.BatteryProposalPdfRequest;
import com.ev_energy_management.backend.exception.AiServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** rul-diagnosis FastAPI 서비스의 /report/pdf/from-view를 호출한다.
 * Agent1~3을 다시 돌리지 않고, 화면에 이미 표시된 값을 그대로 넘겨서 PDF만 렌더링시킨다. */
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
        // 여기서 직접 매핑한다.
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("buyer_name", req.buyerName());
        body.put("buyer_role", req.buyerRole());
        body.put("buyer_location", req.buyerLocation());
        body.put("price_total_manwon", req.priceTotalManwon());
        body.put("unit_price_won", req.unitPriceWon());
        body.put("negotiation_range", req.negotiationRange());
        body.put("price_grade_label", req.priceGradeLabel());
        body.put("price_note", req.priceNote());
        body.put("grade", req.grade());
        body.put("remaining_cycle", req.remainingCycle());
        body.put("new_cycle", req.newCycle());
        body.put("health_score_pct", req.healthScorePct());
        body.put("health_metrics", req.healthMetrics() == null ? List.of() :
                req.healthMetrics().stream()
                        .map(m -> Map.of("label", m.label(), "score", m.score()))
                        .toList());
        body.put("diagnosis_note", req.diagnosisNote());
        body.put("reasons", req.reasons() == null ? List.of() : req.reasons());
        body.put("cautions", req.cautions() == null ? List.of() : req.cautions());

        try {
            byte[] pdf = restClient.post()
                    .uri("/report/pdf/from-view")
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
}
